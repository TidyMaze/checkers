package checkers

object Direction extends Enumeration {
  type Direction = Value
  val TopLeft, TopRight, DownLeft, DownRight = Value

  val toOffset: Map[Direction.Value, Coord] = Map(
    TopLeft -> Coord(-1, -1),
    TopRight -> Coord(1, -1),
    DownLeft -> Coord(-1, 1),
    DownRight -> Coord(1, 1)
  )
}
