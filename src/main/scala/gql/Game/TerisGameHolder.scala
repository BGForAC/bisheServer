package gql.Game

import gql.entity.Player
import gql.server.WebSocketServer

import scala.collection.mutable

object TerisGameHolder {

  val games: mutable.ListBuffer[TerisGame] = mutable.ListBuffer()
  val players: mutable.ListBuffer[Player] = mutable.ListBuffer()

  def init(): Unit = {

  }

  def log(str: String): Unit = {
    println("TerisGameHold:" + str)
  }

  def addPlayer(playerId: String): Unit = {
    players += new Player(playerId)
  }

  def removePlayer(playerId: String): Unit = {
    players.find(_.id == playerId) match {
      case Some(player) =>
        players -= player
        games.find(_.players.exists(_.id == playerId)) match {
          case Some(game) =>
          // 处理玩家中途退出游戏
          case None =>
        }
      case None =>
    }
  }

  def matching(): Unit = {
    log("尝试匹配, 当前匹配玩家数: " + players.size)
    while (players.size >= 2) {
      log("匹配成功，开始游戏")
      // 取出前两个玩家进行匹配
      val gamers = List(players.remove(0), players.remove(0))
      games += new TerisGame(gamers)
      gamers.foreach(player => {
        log(s"玩家 ${player.id} 进入游戏")
        WebSocketServer.sendMessageToClient(player.id, "匹配成功，进入游戏")
        WebSocketServer.sendMessageToClient(player.id, "teris:" + TerisGameHolder.generateRandomBlock(player.id))
      })
    }
  }

  def removeGame(game: TerisGame): Unit = {
    games -= game
  }

  private def getGameByPlayerId(playerId: String): Option[TerisGame] = {
    games.find(_.players.exists(_.id == playerId))
  }

  // 操作模板
  private def actionTemplate(playerId: String, logicName: String): Unit = {
    getGameByPlayerId(playerId) match {
      case Some(game) =>
        game.getClass.getMethod(logicName, classOf[String]).invoke(game, playerId)
      case None =>
        log(s"玩家 $playerId 不在任何游戏中")
        throw new Exception("玩家不在任何游戏中")
    }
  }

  def moveLeft(playerId: String): Unit = actionTemplate(playerId, "moveLeft")

  def moveRight(playerId: String): Unit = actionTemplate(playerId, "moveRight")

  def rotate(playerId: String): Unit = actionTemplate(playerId, "rotate")

  def drop(playerId: String): Unit = actionTemplate(playerId, "drop")

  def onLand(playerId: String): Unit = actionTemplate(playerId, "onLand")

  def checkRow(playerId: String): Unit = actionTemplate(playerId, "checkRow")

  def generateRandomBlock(playerId: String): Int = {
    getGameByPlayerId(playerId) match {
      case Some(game) =>
        game.generateRandomBlock(playerId)
      case None =>
        log(s"玩家 $playerId 不在任何游戏中")
        throw new Exception("玩家不在任何游戏中")
    }
  }
}
