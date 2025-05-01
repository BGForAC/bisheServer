package gql.Game

import scala.collection.mutable
import scala.util.Random

class Teris(game: TerisGame) {
  final val WIDTH = 10
  final val HEIGHT = 14
  final val birthPos = (WIDTH, HEIGHT)

  private var grids = Array.fill(HEIGHT, WIDTH)(0)
  private var currentTeris = 0
  private var point = 0

  def moveLeft(): Unit = {
    // 执行向左移动的逻辑
  }

  def moveRight(): Unit = {

  }

  def rotate(): Unit = {
    // 执行旋转的逻辑
  }

  def onLand(): Unit = {
    // 执行落地的逻辑
  }

  def generateRandomTeris(): Unit = {
    // 生成随机的方块
    currentTeris = Random.nextInt(7)
  }

  def checkRow(row: Int): Unit = {
    // 检查是否有完整的行
  }

  def clearLine(row: Int): Unit = {
    // 执行消除行的逻辑
  }
}
