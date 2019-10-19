package checkers

case class Coord(x: Int, y: Int)

object Coord {
  def add(from: Coord, offset: Coord): Coord = Coord(from.x + offset.x, from.y + offset.y)
}
