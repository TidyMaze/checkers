package checkers

object Grid {
  type Grid = Seq[Seq[Option[Player]]]

  val HEIGHT = 8
  val WIDTH = 8

  def asPrintable2DArray(grid: Grid): Seq[Seq[String]] =
    grid.map(l => l.map {
      case Some(owner) => owner.toString
      case None => "-"
    })
}
