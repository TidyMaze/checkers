package checkers

import java.util.concurrent.atomic.AtomicInteger

import checkers.Game._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object App extends App {

  val store: ListBuffer[(State, Double)] = ListBuffer[(State, Double)]()

  def transformToEploitable(state: State, score: Double): (State, Double) = {
    val theCurrentPlayer = Player.nextPlayer(state.nextPlayer)
    val opp = state.nextPlayer
    val transformedGrid = state.grid.map(line => line.map {
      case `theCurrentPlayer` => 1
      case `opp` => -1
      case _ => 0
    })
    (state.copy(grid = transformedGrid), score)
  }

  def monteCarloEvalFunctionWithStore(samples: Int)(state: State, player: Player, count: AtomicInteger): Double = {
    val score = monteCarloEvalFunction(samples)(state, player, count)
    store += ((state, score))
    score
  }

  val state: State = Game.newGame()

  val turnHandler: (Action, State, Double) => Unit = (action, currentState, score) => {
    println()
    println(s"Playing $action with expected winrate $score for player ${Player.nextPlayer(currentState.nextPlayer)}")
    println(currentState)
  }

  val states = playTillEndWithEvalFunction(state, monteCarloEvalFunctionWithStore(500), turnHandler)
  println(s"after ${states.size} turns")
  states.foreach { s =>
    println(s)
    println()
  }

  val endGameWinner: Option[String] = states.lastOption.flatMap(_.winner.map(_.toString))
  println(s"winner is ${endGameWinner.getOrElse("nobody")}")

  println(s"${store.size} samples")

  store.map {
    case (state, score) => transformToEploitable(state, score)
  }.sortBy(_._2).foreach {
    case (state, score) => println(state.grid.map(_.mkString(" ")).mkString(" ") + " " + score)
  }
}
