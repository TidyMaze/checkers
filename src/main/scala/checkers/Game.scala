package checkers

import checkers.Player.Player1

object Game {
  def newGame(): State = {
    val initPattern = Seq(
      "-2-2-2-2",
      "2-2-2-2-",
      "--------",
      "--------",
      "--------",
      "--------",
      "-1-1-1-1",
      "1-1-1-1-"
    )
    State(
      initPattern.map(line => line.toList.map {
        case '-' => None
        case '1' => Some(Player.Player1)
        case '2' => Some(Player.Player2)
      }),
      Player1
    )
  }
}
