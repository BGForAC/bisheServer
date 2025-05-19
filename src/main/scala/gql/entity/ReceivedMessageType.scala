package gql.entity

object ReceivedMessageType {
  final val LEFT = "left"
  final val RIGHT = "right"
  final val DROP = "drop"
  final val LAND = "land"
  final val ROTATE = "rotate"
  final val GEN = "gen"
  final val CHECK = "check"
  // 开始匹配
  final val START_MATCH = "start match"
  // 取消匹配
  final val CANCEL_MATCH = "cancel match"
}