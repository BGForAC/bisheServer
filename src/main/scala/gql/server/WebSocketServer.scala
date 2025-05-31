package gql.server

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import gql.Game.TerisGameHolder
import gql.entity.{ReceivedMessageType, SendMessageType}

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object WebSocketServer {
  // 连接名到ActorRef的映射
  val connections = TrieMap[String, ActorRef]()
  // 连接名到账号名的映射
  val accounts = TrieMap[String, String]()

  def sendMessageToClient(clientId: String, message: String): Unit = {
    def connectionId: String = {
      if (!accounts.exists(_._2 == clientId)) return ""
      accounts.filter(_._2 == clientId).head._1
    }

    connections.get(connectionId) match {
      case Some(actorRef) =>
        actorRef ! TextMessage(message)
      case None =>
        println(s"玩家 $clientId 未连接, 无法发送消息: $message")
    }
  }

  def log(message: String): Unit = {
    println(message)
  }

  def start()(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext): Unit = {

    def createWebsocketHandler(connectionId: String): Flow[Message, Message, Any] = {
      def clientId: String = {
        accounts.getOrElse(connectionId, "")
      }

      val outgoingMessages: Source[Message, ActorRef] =
        Source.actorRef[Message](bufferSize = 10, OverflowStrategy.dropHead)
          .mapMaterializedValue { actorRef =>
            connections.put(connectionId, actorRef)
            log(s"玩家连接: $connectionId")
            actorRef
          }

      val incomingMessages: Sink[Message, Any] = Sink.foreach {
        case TextMessage.Strict(text) =>
          text match {
            case message if message.startsWith("login") =>
              log("收到登录请求：" + text)
              val clientId = message.split(" ")(1)
              if (TerisGameHolder.validPlayer(clientId)) {
                accounts.put(connectionId, clientId)
                sendMessageToClient(clientId, SendMessageType.LOGIN_SUCCESS)
                log(s"玩家 $clientId 登录成功")
              } else {
                sendMessageToClient(clientId, SendMessageType.LOGIN_FAIL)
                log(s"玩家 $clientId 试图重复登陆")
              }
            case _ =>
              TerisGameHolder.handleMessage(clientId, text)
          }
        case _ =>
          sendMessageToClient(clientId, SendMessageType.UNKNOWN_TYPE)
      }

      Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
        .watchTermination() { (_, termination) =>
          termination.onComplete { _ =>
            val clientId = accounts.getOrElse(connectionId, "")
            if (TerisGameHolder.isInQueue(clientId)) {
              TerisGameHolder.removePlayer(clientId)
              log(s"玩家 $clientId 从匹配队列中移除")
            } else if (TerisGameHolder.isInGame(clientId)) {
              TerisGameHolder.gameOver(clientId)
              log(s"玩家 $clientId 从游戏中移除")
            }

            connections.remove(connectionId)
            accounts.remove(connectionId)
            log(s"玩家 $clientId 断开连接, 链接id: $connectionId")
          }(GameServer.executionContext)
        }
    }

    val route =
      path("ws") {
        extractClientIP { clientIP =>
          handleWebSocketMessages {
            val connectionId = UUID.randomUUID().toString
            createWebsocketHandler(connectionId)
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    log(s"WebSocket server online at ws://localhost:8080/ws")

    system.scheduler.scheduleWithFixedDelay(initialDelay = 0.seconds, delay = 5.seconds) {
      () => heartbeat()
    }(GameServer.executionContext)

    system.scheduler.scheduleWithFixedDelay(initialDelay = 0.seconds, delay = 1.seconds) {
      () => TerisGameHolder.matching()
    }(GameServer.executionContext)

    while (true) {}

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

    def heartbeat(): Unit = {
      connections.foreach { case (connectionId, actorRef) =>
        actorRef ! TextMessage("heartbeat")
      }
      // 打印当前连接的玩家ID
      val playersId = TerisGameHolder.allPlayers()
      println(s"Heartbeat: 当前连接的数量：${connections.size}, 当前登录的玩家数量: ${accounts.size}, 玩家ID: ${accounts.values.mkString(", ")}")
    }
  }
}
