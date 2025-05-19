package gql.Game

import gql.entity.Block

import scala.util.Random

class Teris {
  final val WIDTH = 10
  final val HEIGHT = 22
  final val BIRTH_POINT = (WIDTH / 2, HEIGHT)

  private var game: TerisGame = _
  private var playerId: String = _

  private var grids: Array[Array[Int]] = _
  private var currentTeris: Block = _
  var point: Int = _
  private var offsetX: Int = _
  private var offsetY: Int = _
  private var lastDropTime: Long = _

  private var over: Boolean = false

  def this(game: TerisGame) = {
    this()
    this.game = game
    // 初始化游戏状态
    grids = Array.fill(WIDTH + 1, HEIGHT + 2)(0)
    point = 0
  }

  def this(game: TerisGame, playerId: String) = {
    this(game)
    this.playerId = playerId
  }

  def isOver(): Boolean = {
    over
  }

  def gameOver(): Unit = {
    over = true
  }

  def generateRandomTeris(): Int = {
    lastDropTime = System.currentTimeMillis()
    val number = Random.nextInt(7) + 1
    // 生成随机的方块
    currentTeris = new Block(number, BIRTH_POINT)
    offsetX = 0
    offsetY = 0
    if (outOfRange(currentTeris)) {
      return -1 * number
    }
    number
  }

  def moveLeft(): Unit = {
    // 执行向左移动的逻辑
    offsetX -= 1
  }

  def moveRight(): Unit = {
    // 执行向右移动的逻辑
    offsetX += 1
  }

  def rotate(): Unit = {
    // 执行旋转的逻辑
    currentTeris.rotate()
  }

  def drop(): Unit = {
    // 执行下落的逻辑
    offsetY -= 1
  }

  def outOfRange(block: Block): Boolean = {
    for (child <- block.childsPostions) {
      val (x, y) = child
      if (x + offsetX <= 0 || x + offsetX > WIDTH || y + offsetY <= 0 || grids(x + offsetX)(y + offsetY) == 1) {
        return true
      }
    }
    false
  }

  def onLand(): Unit = {
    // 执行落地的逻辑
    // 将当前方块固定到棋盘上
    for (child <- currentTeris.childsPostions) {
      val (x, y) = child
      if (x + offsetX <= 0 || x + offsetX > WIDTH || y + offsetY <= 0 || grids(x + offsetX)(y + offsetY) == 1) {
        throw new Exception("客户端越界了")
      }
      grids(x + offsetX)(y + offsetY) = 1
    }
    printGrid()
  }

  private def printGrid(): Unit = {
    // 打印当前棋盘状态
    for (j <- HEIGHT to 1 by -1) {
      for (i <- 1 to WIDTH) {
        if (grids(i)(j) == 1) {
          print("X ")
        } else {
          print(". ")
        }
      }
      println()
    }
    println()
  }

  private def updatePoint(count: Int): Unit = {
    count match {
      case 1 =>
        point += 100
      case 2 =>
        point += 300
      case 3 =>
        point += 500
      case 4 =>
        point += 800
    }
  }

  def checkRow(): Unit = {
    // 检查是否有完整的行
    def f(row: Int): Boolean = {
      if (!(1 to WIDTH).forall(i => grids(i)(row) == 1)) {
        return false
      }
      // 如果有完整的行，执行消除行的逻辑
      clearRow(row)
      true
    }

    // 更新分数
    updatePoint((HEIGHT to 1 by -1).count(f))
  }

  private def clearRow(row: Int): Unit = {
    game.clearNotification(playerId, row)
    // 执行消除行的逻辑
    for (j <- row to HEIGHT) {
      for (i <- 1 to WIDTH) {
        grids(i)(j) = grids(i)(j + 1)
      }
    }
  }
}
