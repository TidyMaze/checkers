package checkers

import checkers.Coord.add
import checkers.Direction.toOffset
import checkers.Grid.{Grid, HEIGHT, WIDTH, update2D}
import checkers.Player.{Player1, Player2, nextPlayer}

import scala.util.{Failure, Success, Try}

object Game {

  def newGame(): State = {
    State(
      Grid.initPattern.map(line => line.toList.map {
        case '-' => None
        case '1' => Some(Player1)
        case '2' => Some(Player2)
      }),
      Player1,
      None
    )
  }

  def isInGrid(coord: Coord): Boolean = coord.y >= 0 && coord.x >= 0 && coord.y < HEIGHT && coord.x < WIDTH

  def playAction(action: Action, state: State): Try[State] = {
    val destCoord = add(action.from, toOffset(action.direction))

    if (!isInGrid(destCoord)) {
      Failure(new RuntimeException("Invalid coord outside of grid"))
    } else if (state.grid(destCoord.y)(destCoord.x).isDefined) {
      Failure(new RuntimeException("Invalid coord already occupied"))
    } else {
      val resGrid = move(state.grid, action.from, destCoord, state.nextPlayer)
      Success(State(resGrid, nextPlayer(state.nextPlayer), state.winner))
    }
  }

  def move(grid: Grid, from: Coord, to: Coord, player: Player): Grid = update2D(update2D(grid, from, None), to, Some(player))

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

  def findPiecesCoords(grid: Grid): Map[Player, Seq[Coord]] = {
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

  def playSeveralTurnsRandomly(state: State, turns: Int): Seq[State] = playSeveralTurnsWithEvalFunction(state, turns, _ => RandomHelpers.randPct())

  def playSeveralTurnsWithEvalFunction(state: State, turns: Int, eval: State => Double): Seq[State] = {
    turns match {
      case 0 => Nil
      case remainingTurns =>
        val actions = findAllActions(state)
        if (actions.isEmpty) {
          State(state.grid, state.nextPlayer, Some(nextPlayer(state.nextPlayer))) :: Nil
        } else {
          val (_, newState) = actions.toSeq.maxBy { case (_, state) => eval(state)}
          newState +: playSeveralTurnsRandomly(newState, remainingTurns - 1)
        }
    }
  }

  def basicEvalFunction(state: State): Double = {
    val pieces = Game.findPiecesCoords(state.grid)
    pieces(state.nextPlayer).size - pieces(Player.nextPlayer(state.nextPlayer)).size
  }
}
