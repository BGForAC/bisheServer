package gql.server

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import gql.Game.TerisGameHolder

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object WebSocketServer {
  val connections = TrieMap[String, ActorRef]()

  def sendMessageToClient(clientId: String, message: String): Unit = {
    connections.get(clientId) match {
      case Some(actorRef) =>
        actorRef ! TextMessage(message)
      case None =>
        println(s"Client $clientId not found in connections")
    }
  }

  def start()(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext): Unit = {
    def createWebsocketHandler(clientId: String): Flow[Message, Message, Any] = {
      val outgoingMessages: Source[Message, ActorRef] =
        Source.actorRef[Message](bufferSize = 10, OverflowStrategy.dropHead)
          .mapMaterializedValue { actorRef =>
            connections.put(clientId, actorRef)
            actorRef
          }

      val incomingMessages: Sink[Message, Any] = Sink.foreach {
        case TextMessage.Strict(text) =>
          text match {
//            case "login" + clientId =>
//              if (TerisGameHolder.checkPlayer(playerId)){
//
//              } else {
//                TerisGameHolder.login()
//              }
            case "left" =>
              TerisGameHolder.moveLeft(clientId)
              sendMessageToClient(clientId, "moving left")
            case "right" =>
              TerisGameHolder.moveRight(clientId)
              sendMessageToClient(clientId, "moving right")
            case "drop" =>
              TerisGameHolder.drop(clientId)
              sendMessageToClient(clientId, "dropping")
            case "land" =>
              TerisGameHolder.onLand(clientId)
              sendMessageToClient(clientId, "landing")
            case "rotate" =>
              TerisGameHolder.rotate(clientId)
              sendMessageToClient(clientId, "rotating")
            case "gen" =>
              val number = TerisGameHolder.generateRandomBlock(clientId)
              sendMessageToClient(clientId, "teris:" + Math.abs(number))
              if (number < 0) {
                sendMessageToClient(clientId, "gameOver")
                TerisGameHolder.gameOver(clientId)
              }
            case "clear" =>
              TerisGameHolder.checkRow(clientId)
              sendMessageToClient(clientId, "clear")
            case _ =>
              sendMessageToClient(clientId, "Unknown command" + text)
          }
        case _ =>
          TextMessage("Unsupported message type")
      }

      Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
    }

    val route =
      path("ws") {
        extractClientIP { clientIP =>
          handleWebSocketMessages {
            val clientId = UUID.randomUUID().toString
            val flow = createWebsocketHandler(clientId)
            TerisGameHolder.addPlayer(clientId)
            println(s"Client connected: $clientId")
            flow
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"WebSocket server online at ws://localhost:8080/ws")

    system.scheduler.scheduleWithFixedDelay(initialDelay = 0.seconds, delay = 5.seconds) {
      () => heartbeat()
    }(GameServer.executionContext)

    system.scheduler.scheduleWithFixedDelay(initialDelay = 0.seconds, delay = 5.seconds) {
      () => TerisGameHolder.matching()
    }(GameServer.executionContext)

    while (true) {}

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

    def heartbeat(): Unit = {
      connections.foreach { case (clientId, actorRef) =>
        actorRef ! TextMessage("heartbeat")
        val playersId = TerisGameHolder.games.flatMap(_.games.keys) ++ TerisGameHolder.players.map(_.id)
        // 打印当前连接的玩家ID
        println(s"Heartbeat: 当前玩家数量: ${playersId.size}, 玩家ID: ${playersId.mkString(", ")}")
      }
    }
  }
}
