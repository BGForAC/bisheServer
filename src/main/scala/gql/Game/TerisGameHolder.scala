package gql.Game

import gql.entity.{Player, ReceivedMessageType, SendMessageType}
import gql.server.WebSocketServer

import scala.collection.mutable

object TerisGameHolder {
  var games: mutable.ListBuffer[TerisGame] = _
  var queue: mutable.ListBuffer[Player] = _

  def allPlayers(): mutable.ListBuffer[String] = {
    games.flatMap(_.games.keys) ++ queue.map(_.id)
  }

  def validPlayer(playerId: String): Boolean = {
    !allPlayers().contains(playerId)
  }

  def init(): Unit = {
    games = mutable.ListBuffer[TerisGame]()
    queue = mutable.ListBuffer[Player]()
  }

  def isInQueue(playerId: String): Boolean = {
    queue.exists(_.id == playerId)
  }

  def isInGame(playerId: String): Boolean = {
    games.exists(_.players.exists(_.id == playerId))
  }

  def handleMessage(playerId: String, message: String): Unit = {
    message match {
      case ReceivedMessageType.START_MATCH =>
        enqueuePlayer(playerId)
      case ReceivedMessageType.CANCEL_MATCH =>
        dequeuePlayer(playerId)
      case ReceivedMessageType.LEFT =>
        moveLeft(playerId)
      case ReceivedMessageType.RIGHT =>
        moveRight(playerId)
      case ReceivedMessageType.DROP =>
        drop(playerId)
      case ReceivedMessageType.LAND =>
        onLand(playerId)
      case ReceivedMessageType.ROTATE =>
        rotate(playerId)
      case ReceivedMessageType.GEN =>
        val number = generateRandomBlock(playerId)
        WebSocketServer.sendMessageToClient(playerId, SendMessageType.GEN(number))
        if (number < 0) {
          log(s"玩家 $playerId 游戏结束")
          gameOver(playerId)
          WebSocketServer.sendMessageToClient(playerId, SendMessageType.GAME_OVER)
          val game = getGameByPlayerId(playerId).get
          if (game.games.forall(_._2.isOver())) {
            game.onDestory()
            removeGame(game)
          }
        }
      case ReceivedMessageType.CHECK =>
        checkRow(playerId)
      case _ =>
        log(s"收到未知消息: $message")
        throw new Exception("服务器收到了未知类型消息")
    }
  }

  def log(str: String): Unit = {
    println("TerisGameHolder:" + str)
  }

  private def enqueuePlayer(playerId: String): Unit = {
    queue += Player(playerId)
  }

  private def dequeuePlayer(playerId: String): Unit = {
    queue.find(_.id == playerId) match {
      case Some(player) =>
        queue -= player
      case None =>
        log(s"玩家 $playerId 已不在匹配队列中")
    }
  }

  def removePlayer(playerId: String): Unit = {
    queue.find(_.id == playerId) match {
      case Some(player) =>
        queue -= player
        games.find(_.players.exists(_.id == playerId)) match {
          case Some(game) =>
          // 处理玩家中途退出游戏
          case None =>
        }
      case None =>
    }
  }

  def matching(): Unit = {
    log("尝试匹配, 当前匹配玩家数: " + queue.size)
    while (queue.nonEmpty) {
      var gamers = List[Player]()
      if (queue.size >= 4) {
        gamers = List(queue.remove(0), queue.remove(0), queue.remove(0), queue.remove(0))
      } else if (queue.size > 1){
        gamers = queue.toList
        queue.clear()
      } else {
        log("匹配玩家不足，退出匹配")
        return
      }
      log("匹配成功，开始游戏")
      games += new TerisGame(gamers)
      gamers.foreach(player => {
        log(s"玩家 ${player.id} 进入游戏")
        val message: String = SendMessageType.MATCH_SUCCESS + " " + gamers.filter(_.id != player.id).map(_.id).mkString(",")
        WebSocketServer.sendMessageToClient(player.id, message)
      })
      // 等待2s再开始游戏
      Thread.sleep(1000)
      gamers.foreach(player => {
        WebSocketServer.sendMessageToClient(player.id, SendMessageType.GEN(generateRandomBlock(player.id)))
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

  def gameOver(playerId: String): Unit = actionTemplate(playerId, "gameOver")

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
