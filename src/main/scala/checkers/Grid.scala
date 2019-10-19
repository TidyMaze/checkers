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

  def printableGrids(grids: Seq[Grid]): String = {
    val displayableGrids = grids.map(asPrintable2DArray)
    (for {
      y <- 0 until HEIGHT
      eachLineFromGrids = displayableGrids.map(_(y)).map(_.mkString(" ")).mkString("\t\t")
    } yield eachLineFromGrids).mkString("\n")
  }

  val initPattern: Seq[String] = Seq(
    "-2-2-2-2",
    "2-2-2-2-",
    "--------",
    "--------",
    "--------",
    "--------",
    "-1-1-1-1",
    "1-1-1-1-"
  )

  def update2D[A](matrix: Seq[Seq[A]], coord: Coord, elem: A): Seq[Seq[A]] =
    matrix.updated(coord.y, matrix(coord.y).updated(coord.x, elem))
}
