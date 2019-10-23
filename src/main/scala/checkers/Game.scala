package checkers

import checkers.Coord.add
import checkers.Direction.toOffset
import checkers.Grid.{Grid, HEIGHT, WIDTH, update2D}
import checkers.Player.nextPlayer
import checkers.RandomHelpers._

import scala.annotation.tailrec
import scala.collection.SeqView
import scala.util.{Failure, Success, Try}

object Game {
  type Player = Int

  def newGame(): State = {
    State(
      Grid.initPattern.map(line => line.map {
        case '-' => 0
        case '1' => 1
        case '2' => 2
      }),
      1,
      None
    )
  }

  def playTillEndRandomlyNoHistory(state: State, onTurn: State => Unit): State =
    playTillEndWithEvalFunctionNoHistory(state, (_, _) => randPct, onTurn)

  def playTillEndWithEvalFunction(state: State, eval: (State, Player) => Double, onTurn: (Action, State, Double) => Unit = (_, _, _) => ()): List[State] = {
    @tailrec
    def aux(s: State, acc: List[State]): List[State] = {
      val actions = findAllActions(s).toMap
      if (actions.isEmpty) {
        acc.reverse
      } else {
        val (action, newState, score) = actions.par.map { case (a, candidateState) => (a, candidateState, eval(candidateState, s.nextPlayer)) }.maxBy { case (a, candidateState, score) => score }
        onTurn(action, newState, score)
        aux(newState, newState +: acc)
      }
    }

    aux(state, Nil)
  }

  def playTillEndWithEvalFunctionNoHistory(state: State, eval: (State, Player) => Double, onTurn: State => Unit = _ => ()): State = {
    @tailrec
    def aux(s: State): State = {
      val actions = findAllActions(s)
      if (actions.isEmpty) {
        s
      } else {
        val newState = actions.head._2
        onTurn(newState)
        aux(newState)
      }
    }

    aux(state)
  }

  def findAllActions(state: State): SeqView[(Action, State), Seq[_]] = {
    for {
      from <- shuffle(findPiecesCoords(state.grid).getOrElse(state.nextPlayer, Nil)).view
      dir <- shuffle(Direction.values.toSeq)
      action = Action(from, dir)
      maybeResState = playAction(action, state).toOption
      resState <- maybeResState if maybeResState.isDefined
    } yield action -> resState
  }

  def playAction(action: Action, state: State): Try[State] = {
    val offset = toOffset(action.direction)
    val destCoord = add(action.from, offset)

    if (!isInGrid(destCoord)) {
      Failure(new RuntimeException("Invalid coord outside of grid"))
    } else {
      val target1 = state.grid(destCoord.y)(destCoord.x)
      if (target1 != 0) {
        val destCoord2 = add(destCoord, offset)
        if (!isInGrid(destCoord2)) {
          Failure(new RuntimeException("Invalid coord outside of grid"))
        } else if (target1 == state.nextPlayer) {
          Failure(new RuntimeException("Cannot jump same player"))
        } else if (state.grid(destCoord2.y)(destCoord2.x) != 0) {
          Failure(new RuntimeException("Invalid coord already occupied"))
        } else {
          val resGrid2 = jump(state.grid, action.from, destCoord, destCoord2, state.nextPlayer)
          Success(State(resGrid2, nextPlayer(state.nextPlayer), winner(resGrid2)))
        }
      } else {
        val resGrid = move(state.grid, action.from, destCoord, state.nextPlayer)
        Success(State(resGrid, nextPlayer(state.nextPlayer), winner(resGrid)))
      }
    }
  }

  def isInGrid(coord: Coord): Boolean = coord.y >= 0 && coord.x >= 0 && coord.y < HEIGHT && coord.x < WIDTH

  def winner(grid: Grid): Option[Player] = {
    val playersPieces = findPiecesCoords(grid)
    if (!playersPieces.isDefinedAt(1)) Some(2)
    else if (!playersPieces.isDefinedAt(2)) Some(1)
    else None
  }

  def move(grid: Grid, from: Coord, to: Coord, player: Player): Grid = update2D(update2D(grid, from, 0), to, player)

  def jump(grid: Grid, from: Coord, jumped: Coord, to: Coord, player: Player): Grid = {
    eat(move(grid, from, to, player), jumped)
  }

  def eat(grid: Grid, coord: Coord): Grid = update2D(grid, coord, 0)

  def findPiecesCoords(grid: Grid): Map[Player, Seq[Coord]] = {
    (for {
      y <- 0 until HEIGHT
      x <- 0 until WIDTH
      line <- grid.lift(y)
      cell <- line.lift(x) if cell != 0
    } yield (cell, Coord(x, y)))
      .groupBy { case (player, _) => player }
      .mapValues(_.map { case (_, coord) => coord })
  }

  def basicEvalFunction(state: State, player: Player): Double = {
    val pieces = Game.findPiecesCoords(state.grid)
    pieces.getOrElse(player, Nil).size - pieces.getOrElse(Player.nextPlayer(player), Nil).size
  }

  def monteCarloEvalFunction(samples: Int)(state: State, player: Player): Double = {
    val allWinnersGrouped = (0 until samples).par.map { iGame =>
//      print(".")
      playTillEndRandomlyNoHistory(state, _ => ()).winner
    }.groupBy(identity).mapValues(_.size)
    print("_")
    (allWinnersGrouped.getOrElse(Some(player), 0).toDouble + allWinnersGrouped.getOrElse(None, 0).toDouble / 2) / samples.toDouble
  }
}
