package gql.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import gql.Game.TerisGameHolder

object GameServer extends Runnable {
  override def run(): Unit = {
    implicit val system: ActorSystem = ActorSystem("game-server-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    // 初始化游戏逻辑
    TerisGameHolder.init()

    // 启动WebSocket服务器
    WebSocketServer.start()
  }
}
