package checkers

import checkers.Game.Player
import checkers.Grid.{Grid, asPrintable2DArray}
import scala.collection.immutable.Seq

case class State(grid: Grid, nextPlayer: Player, playersPieces: Map[Player, Seq[Coord]], winner: Option[Player]) {
  override def toString: String = asPrintable2DArray(grid).map(_.mkString(" ")).mkString("\n")
}

