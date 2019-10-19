package checkers

case class State(grid: Seq[Seq[Option[Player]]], nextPlayer: Player) {
  override def toString: String = asPrintable2DArray.map(_.mkString("")).mkString("\n")

  def asPrintable2DArray: Seq[Seq[String]] =
    grid.map(l => l.map {
      case Some(owner) => owner.toString
      case None => "-"
    })
}

