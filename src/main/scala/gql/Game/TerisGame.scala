package gql.Game

import gql.entity.Player
import scala.collection.mutable

// 一局游戏有多个玩家，棋盘彼此独立
class TerisGame(val players: List[Player]) {

  private val games = mutable.Map[String, Teris]()

  init()

  def log(str: String): Unit = {
    println(str)
  }

  def init(): Unit = {
    games.put(players(0).id, new Teris(this))
    games.put(players(1).id, new Teris(this))
  }

  def moveLeft(playerId: String): Unit = {
    games.get(playerId) match {
      case Some(game) =>
        // 执行向左移动的逻辑
        log(s"玩家 $playerId 向左移动")
        game.moveLeft()
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
  }

  def moveRight(playerId: String): Unit = {
    games.get(playerId) match {
      case Some(game) =>
        // 执行向右移动的逻辑
        log(s"玩家 $playerId 向右移动")
        game.moveRight()
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
  }

  def rotate(playerId: String): Unit = {
    games.get(playerId) match {
      case Some(game) =>
        // 执行旋转的逻辑
        log(s"玩家 $playerId 旋转")
        game.rotate()
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
  }

  def onLand(playerId: String): Unit = {
    games.get(playerId) match {
      case Some(game) =>
        // 执行落地的逻辑
        log(s"玩家 $playerId 落地")
        game.onLand()
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
  }

  def generateRandomBlock(playerId: String): Unit = {
    games.get(playerId) match {
      case Some(game) =>
        // 生成随机方块的逻辑
        log(s"玩家 $playerId 生成随机方块")
        game.generateRandomTeris()
      case None =>
        log(s"玩家 $playerId 的游戏不存在")
    }
  }
}
