package checkers

import checkers.Game.Player

object Player {

  val nextPlayer: Player => Player = {
    case 1 => 2
    case 2 => 1
  }
}
