package checkers

import checkers.Coord.add
import checkers.Direction.toOffset
import checkers.Grid.{Grid, HEIGHT, WIDTH}
import checkers.Player.{Player1, Player2}

import scala.util.{Failure, Success, Try}

object Game {

  def newGame(): State = {
    val initPattern = Seq(
      "-2-2-2-2",
      "2-2-2-2-",
      "--------",
      "--------",
      "--------",
      "--------",
      "-1-1-1-1",
      "1-1-1-1-"
    )
    State(
      initPattern.map(line => line.toList.map {
        case '-' => None
        case '1' => Some(Player1)
        case '2' => Some(Player2)
      }),
      Player1
    )
  }

  val nextPlayer: Player => Player = {
    case Player1 => Player2
    case Player2 => Player1
  }

  def isInGrid(coord: Coord): Boolean = coord.y >= 0 && coord.x >= 0 && coord.y < HEIGHT && coord.x < WIDTH

  def playAction(action: Action, state: State): Try[State] = {
    val destCoord = add(action.from, toOffset(action.direction))

    if (!isInGrid(destCoord)) {
      Failure(new RuntimeException("Invalid coord outside of grid"))
    } else if(state.grid(destCoord.y)(destCoord.x).isDefined) {
      Failure(new RuntimeException("Invalid coord already occupied"))
    }else {
      val resGrid = move(state.grid, action.from, destCoord, state.nextPlayer)
      Success(State(resGrid, nextPlayer(state.nextPlayer)))
    }
  }

  def move(grid: Seq[Seq[Option[Player]]], from: Coord, to: Coord, player: Player): Grid = update2D(update2D(grid, from, None), to, Some(player))

  def update2D[A](matrix: Seq[Seq[A]], coord: Coord, elem: A): Seq[Seq[A]] =
    matrix.updated(coord.y, matrix(coord.y).updated(coord.x, elem))

  def findAllActions(state: State): Map[Action, State] = {
    val player = state.nextPlayer
    val playerPieces = findPiecesCoords(state.grid)(player)

    (for {
      from <- playerPieces
      dir <- Direction.values
      action = Action(from, dir)
      maybeResState = playAction(action, state).toOption
      resState <- maybeResState if maybeResState.isDefined
    } yield action -> resState).toMap
  }

  def findPiecesCoords(grid: Seq[Seq[Option[Player]]]): Map[Player, Seq[Coord]] = {
    (for {
      y <- 0 until HEIGHT
      x <- 0 until WIDTH
      line <- grid.lift(y)
      cell <- line.lift(x)
      player <- cell if cell.isDefined
    } yield (player, Coord(x, y)))
      .groupBy { case (player, _) => player }
      .mapValues(_.map { case (_, coord) => coord })
  }
}
