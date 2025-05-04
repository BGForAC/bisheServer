package gql.entity

import scala.collection.mutable

class Block {

  private var pos: (Int, Int) = (0, 0)

  private val childs: mutable.ListBuffer[(Int, Int)] = mutable.ListBuffer()

  def childsPostions: List[(Int, Int)] = {
    childs.map { case (x, y) => (x + pos._1, y + pos._2) }.toList
  }

  def this(number: Int) = {
    this()
    number match {
      // 方块T
      case 1 =>
        childs ++= mutable.ListBuffer((0, 0), (1, 0), (-1, 0), (0, -1))
      // 方块Z
      case 2 =>
        childs ++= mutable.ListBuffer((0, 0), (-1, 0), (0, -1), (1, -1))
      // 方块S
      case 3 =>
        childs ++= mutable.ListBuffer((0, 0), (1, 0), (0, -1), (-1, -1))
      // 方块L
      case 4 =>
        childs ++= mutable.ListBuffer((0, 0), (0, 1), (0, -1), (1, -1))
      // 方块J
      case 5 =>
        childs ++= mutable.ListBuffer((0, 0), (0, -1), (0, 1), (-1, -1))
      // 方块O
      case 6 =>
        childs ++= mutable.ListBuffer((0, 0), (1, 0), (0, -1), (1, -1))
      // 方块I
      case 7 =>
        childs ++= mutable.ListBuffer((0, 0), (0, -1), (0, -2), (0, 1))
      case other =>
        println(s"未知的方块类型: $other")
    }
  }

  def this(number:Int, birthPoint: (Int, Int)) = {
    this(number)
    this.pos = birthPoint
  }

  def rotate(): Unit = {
    // 旋转逻辑
    for (i <- childs.indices) {
      val (x, y) = childs(i)
      childs(i) = (-y, x)
    }
  }


}
