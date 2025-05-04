package gql.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import gql.Game.TerisGameHolder

object GameServer extends Runnable {
  implicit val system: ActorSystem = ActorSystem("game-server-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  override def run(): Unit = {
    // 初始化游戏逻辑
    TerisGameHolder.init()

    // 启动WebSocket服务器
    WebSocketServer.start()
  }
}
