package checkers

import java.util.concurrent.atomic.AtomicInteger

import checkers.Coord.add
import checkers.Direction.{Direction, toOffset}
import checkers.Grid.{Grid, HEIGHT, WIDTH, update2D}
import checkers.Player.nextPlayer
import checkers.RandomHelpers._

import scala.annotation.tailrec
import scala.collection.immutable.Seq
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Game {
  type Player = Int

  def newGame(): State = {
    val grid = Grid.initPattern.map(line => line.map {
      case '-' => 0
      case '1' => 1
      case '2' => 2
    })
    State(grid, 1, findPiecesCoords(grid), None)
  }

  def playTillEndRandomlyNoHistory(state: State, onTurn: State => Unit, count: AtomicInteger): State =
    playTillEndWithEvalFunctionNoHistory(state, (_, _) => randPct, onTurn, count)

  def playTillEndWithEvalFunction(state: State, eval: (State, Player, AtomicInteger) => Double, onTurn: (Action, State, Double) => Unit = (_, _, _) => ()): List[State] = {
    @tailrec
    def aux(s: State, acc: List[State]): List[State] = {
      val count = new AtomicInteger()
      val start = System.currentTimeMillis()
      val actions = findAllActions(s).toMap
      if (actions.isEmpty) {
        acc.reverse
      } else {
        val (action, newState, score) = actions.par.map { case (a, candidateState) => (a, candidateState, eval(candidateState, s.nextPlayer, count)) }.maxBy { case (a, candidateState, score) => score }
        val end = System.currentTimeMillis()
        val elapsed = (end - start) / 1000.0
        println()
        println(s"spent $elapsed sec for ${count.get} turns (${count.get / elapsed} t/sec)")
        onTurn(action, newState, score)
        aux(newState, newState +: acc)
      }
    }

    aux(state, Nil)
  }

  def playTillEndWithEvalFunctionNoHistory(state: State, eval: (State, Player) => Double, onTurn: State => Unit = _ => (), count: AtomicInteger): State = {
    @tailrec
    def aux(s: State): State = {
      val actions = findOneRandomAction(s)
      if (actions.isEmpty) {
        s
      } else {
        val newState = actions.head._2
        count.incrementAndGet()
        onTurn(newState)
        aux(newState)
      }
    }

    aux(state)
  }

  def findAllActions(state: State): Seq[(Action, State)] = {
    for {
      from <- RandomHelpers.random.shuffle(state.playersPieces.getOrElse(state.nextPlayer, Seq.empty))
      dir <- RandomHelpers.random.shuffle(Direction.values.to[Seq])
      action = Action(from, dir)
      maybeResState = playAction(action, state)
      resState <- maybeResState if maybeResState.isDefined
    } yield action -> resState
  }

  def findOneRandomAction(state: State): Option[(Action, State)] = {
    val coords = state.playersPieces.getOrElse(state.nextPlayer, Nil)

    var allCombinations = new ArrayBuffer[(Coord, Direction)](coords.size * Direction.values.size)
    coords.foreach(c =>
      Direction.values.foreach(d =>
        allCombinations.append((c, d))
      )
    )
    RandomHelpers.shuffle(allCombinations)

    var found: (Action, State) = null
    var over = false
    while(!over && found == null){
      if(allCombinations.isEmpty){
        over = true
      } else {
        val (from, dir) = allCombinations.last
        allCombinations.reduceToSize(allCombinations.size - 1)
        val action = Action(from, dir)
        playAction(action, state) match {
          case Some(s) => found = (action, s)
          case None    => ()
        }
      }
    }

    Option(found)
  }

  def playAction(action: Action, state: State): Option[State] = {
    val offset = toOffset(action.direction)
    val destCoord = add(action.from, offset)

    if (!isInGrid(destCoord)) {
      None
    } else {
      val target1 = state.grid(destCoord.y)(destCoord.x)
      if (target1 != 0) {
        val destCoord2 = add(destCoord, offset)
        if (!isInGrid(destCoord2)) {
          None
        } else if (target1 == state.nextPlayer) {
          None
        } else if (state.grid(destCoord2.y)(destCoord2.x) != 0) {
          None
        } else {
          val resGrid2 = jump(state.grid, action.from, destCoord, destCoord2, state.nextPlayer)
          val pieces = findPiecesCoords(resGrid2)
          Some(State(resGrid2, nextPlayer(state.nextPlayer), pieces, winner(pieces)))
        }
      } else {
        val resGrid = move(state.grid, action.from, destCoord, state.nextPlayer)
        val pieces = findPiecesCoords(resGrid)
        Some(State(resGrid, nextPlayer(state.nextPlayer), pieces, state.winner))
      }
    }
  }

  def isInGrid(coord: Coord): Boolean = coord.y >= 0 && coord.x >= 0 && coord.y < HEIGHT && coord.x < WIDTH

  def winner(pieces: Map[Int, Seq[Coord]]): Option[Player] = {
    if (!pieces.isDefinedAt(1)) Some(2)
    else if (!pieces.isDefinedAt(2)) Some(1)
    else None
  }

  def move(grid: Grid, from: Coord, to: Coord, player: Player): Grid = update2D(update2D(grid, from, 0), to, player)

  def jump(grid: Grid, from: Coord, jumped: Coord, to: Coord, player: Player): Grid = {
    eat(move(grid, from, to, player), jumped)
  }

  def eat(grid: Grid, coord: Coord): Grid = update2D(grid, coord, 0)

  def findPiecesCoords(grid: Grid): Map[Player, Seq[Coord]] = {
    var res = new mutable.HashMap[Player, List[Coord]]()
    var y = 0
    while(y < HEIGHT){
      var x = 0
      while(x < WIDTH){
        val cell = grid(y)(x)
        if(cell != 0){
          res.put(cell, Coord(x,y) :: res.getOrElse(cell, Nil))
        }
        x += 1
      }
      y += 1
    }

    res.toMap
  }

  def monteCarloEvalFunction(samples: Int)(state: State, player: Player, count: AtomicInteger): Double = {
    val (res, resDraw) = (0 until samples).foldLeft((0,0)) { case ((c, cDraw), _) =>
//      print(".")
      playTillEndRandomlyNoHistory(state, _ => (), count).winner match {
        case Some(`player`) => (c + 1, cDraw)
        case None => (c, cDraw + 1)
        case _ => (c, cDraw)
      }
    }
    print("_")
    (res.toDouble + resDraw.toDouble / 2) / samples.toDouble
  }
}
