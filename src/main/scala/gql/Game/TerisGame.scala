package gql.Game

import gql.entity.{Player, SendMessageType}
import gql.server.WebSocketServer

import scala.collection.mutable

// 一局游戏有多个玩家，棋盘彼此独立
class TerisGame {
  val players = mutable.ListBuffer[Player]()

  val games = mutable.Map[String, Teris]()

  def log(str: String): Unit = {
    println("TerisGame:" + str)
  }

  def onDestory(): Unit = {
    log("销毁游戏, 打印游戏结果")
    val scores = games.map { case (playerId, game) =>
      val score = game.point
      playerId -> score
    }.toSeq
    scores.sortBy(_._2).reverse.foreach { case (playerId, score) =>
      log(s"玩家 $playerId 的分数: $score")
    }
  }

  def this(players: List[Player]) = {
    this()
    log("初始化游戏")
    this.players ++= players
    players.foreach { player =>
      log(s"初始化玩家 ${player.id} 的游戏")
      // 初始化游戏逻辑
      games.put(player.id, new Teris(this, player.id))
    }
  }

  // 无参操作模板
  private def npActionTemplate(playerId: String, operationName: String, logicName: String): Unit = {
    games.get(playerId) match {
      case Some(game) =>
        if (game.isOver()) {
          log(s"玩家 $playerId 在游戏结束后出现操作")
          return
        }
        // 执行游戏逻辑
        log(s"玩家 $playerId 执行游戏逻辑: $operationName")
        // 反射调用游戏逻辑
        game.getClass.getMethod(logicName).invoke(game)
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
    // 通知游戏内的其他玩家
    notifyOthers(playerId, operationName, logicName)
  }

  def notifyOthers(playerId: String, operationName: String, logicName: String): Unit = {
    players.filter(_.id != playerId).foreach { player =>
      log(s"通知玩家 ${player.id} 玩家 $playerId 执行游戏逻辑: $operationName")
      WebSocketServer.sendMessageToClient(player.id, SendMessageType.OPPONENT_OPERATION(playerId, logicName))
    }
  }

  def moveLeft(playerId: String): Unit = npActionTemplate(playerId, "左移", "moveLeft")

  def moveRight(playerId: String): Unit = npActionTemplate(playerId, "右移", "moveRight")

  def rotate(playerId: String): Unit = npActionTemplate(playerId, "旋转", "rotate")

  def onLand(playerId: String): Unit = npActionTemplate(playerId, "落地", "onLand")

  def drop(playerId: String): Unit = npActionTemplate(playerId, "下落", "drop")

  def checkRow(playerId: String): Unit = npActionTemplate(playerId, "检查行", "checkRow")

  def gameOver(playerId: String): Unit = npActionTemplate(playerId, "游戏结束", "gameOver")

  def clearNotification(playerId: String, row: Int): Unit = notifyOthers(playerId, "清除行", "clearRow:" + row)

  def generateRandomBlock(playerId: String): Int = {
    var number: Int = 0
    games.get(playerId) match {
      case Some(game) =>
        // 生成随机方块的逻辑
        log(s"玩家 $playerId 生成随机方块")
        number = game.generateRandomTeris()
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
    // 通知游戏内的其他玩家
    players.filter(_.id != playerId).foreach { player =>
      log(s"通知玩家 ${player.id} 玩家 $playerId 生成随机方块")
      WebSocketServer.sendMessageToClient(player.id, SendMessageType.OPPONENT_OPERATION(playerId, SendMessageType.GEN(number)))
    }
    number
  }
}
