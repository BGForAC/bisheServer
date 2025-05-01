package gql.Game

import gql.entity.Player
import scala.collection.mutable

object TerisGameHolder {

  private val games: mutable.ListBuffer[TerisGame] = mutable.ListBuffer()
  val players: mutable.ListBuffer[Player] = mutable.ListBuffer()

  def init(): Unit = {

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
    while (players.size >= 2) {
      val player1 = players.remove(0)
      val player2 = players.remove(0)
      val game = new TerisGame(List(player1, player2))
      games += game
    }
  }

  def removeGame(game: TerisGame): Unit = {
    games -= game
  }

  private def getGameByPlayerId(playerId: String): Option[TerisGame] = {
    games.find(_.players.exists(_.id == playerId))
  }

  def moveLeft(playerId: String): Unit = {
    getGameByPlayerId(playerId) match {
      case Some(game) =>
        game.moveLeft(playerId)
      case None =>
        println(s"玩家 $playerId 不在任何游戏中")
    }
  }

  def moveRight(playerId: String): Unit = {
    getGameByPlayerId(playerId) match {
      case Some(game) =>
        game.moveRight(playerId)
      case None =>
        println(s"玩家 $playerId 不在任何游戏中")
    }
  }

  def rotate(playerId: String): Unit = {
    getGameByPlayerId(playerId) match {
      case Some(game) =>
        game.rotate(playerId)
      case None =>
        println(s"玩家 $playerId 不在任何游戏中")
    }
  }

  def onLand(playerId: String): Unit = {
    getGameByPlayerId(playerId) match {
      case Some(game) =>
        game.onLand(playerId)
      case None =>
        println(s"玩家 $playerId 不在任何游戏中")
    }
  }
}
