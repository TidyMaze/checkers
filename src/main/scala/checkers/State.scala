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
}

case class State(grid: Seq[Seq[Option[Player]]], nextPlayer: Player) {
  override def toString: String = grid.map(l => l.map {
    case Some(owner) => owner.toString
    case None => "-"
  }.mkString("")).mkString("\n")
}

object Grid {
  val HEIGHT = 8
  val WIDTH = 8
}