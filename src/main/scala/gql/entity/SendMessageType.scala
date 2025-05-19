package gql.entity

object SendMessageType {
  final val MOVE_LEFT = "moving left"
  final val MOVE_RIGHT = "moving right"
  final val DROP = "dropping"
  final val ROTATE = "rotating"
  final val LAND = "landing"
  final val GAME_OVER = "game over"
  final val CLEAR = "clear"
  final val UNKNOWN_TYPE = "服务器收到了未知类型消息"
  final val LOGIN_SUCCESS = "login success"
  final val LOGIN_FAIL = "login failed"
  final val MATCH_SUCCESS = "match success"

  final def UNKNOWN_COMMAND(content: String): String = {
    "服务器收到了未知的命令" + content
  }
  def GEN(number: Int): String = {
    "teris:" + Math.abs(number)
  }

  // 对手的操作
  def OPPONENT_OPERATION(playerId: String, operation: String): String = {
    s"* $playerId $operation"
  }
}
