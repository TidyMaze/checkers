package checkers

import checkers.Player.{Player1, Player2}

sealed trait Player {
  override def toString: String = this match {
    case Player1 => "1"
    case Player2 => "2"
  }
}

object Player {

  case object Player1 extends Player

  case object Player2 extends Player

  val nextPlayer: Player => Player = {
    case Player1 => Player2
    case Player2 => Player1
  }
}
