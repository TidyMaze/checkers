package checkers

case class State(grid: Seq[Seq[Option[Player]]], nextPlayer: Player) {
  override def toString: String = grid.map(l => l.map {
    case Some(owner) => owner.toString
    case None => "-"
  }.mkString("")).mkString("\n")
}

