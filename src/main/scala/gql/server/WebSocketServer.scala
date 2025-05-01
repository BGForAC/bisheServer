package gql.server

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.collection.mutable

object WebSocketServer {
  private val connections = mutable.Set[Sink[Message, Any]]()

  def start()(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext): Unit = {
    // WebSocket handler
    val websocketHandler: Flow[Message, Message, Any] = Flow[Message]
      .map {
        case TextMessage.Strict(text) =>
          TextMessage(s"Server received: $text")
        case _ =>
          TextMessage("Unsupported message type")
      }
      .alsoTo(Sink.foreach { message =>
        // Register connections
        connections.add(Sink.ignore)
      })

    // Define the route
    val route =
      path("ws") {
        handleWebSocketMessages(websocketHandler)
      }

    // Start the server
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"WebSocket server online at ws://localhost:8080/ws")

    // Start heartbeat
    startHeartbeat()

    while(true) {}

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

    def startHeartbeat()(implicit system: ActorSystem): Cancellable = {
      system.scheduler.scheduleAtFixedRate(1.second, 1.second) { () =>
        val heartbeatMessage = TextMessage("Heartbeat")
        connections.foreach { connection =>
          connection.runWith(Source.single(heartbeatMessage))
        }
      }
    }

  }

}
