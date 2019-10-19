package checkers

import checkers.Grid.{Grid, asPrintable2DArray}

case class State(grid: Grid, nextPlayer: Player, winner: Option[Player]) {
  override def toString: String = asPrintable2DArray(grid).map(_.mkString(" ")).mkString("\n")
}

