package checkers

import java.io.{File, FileWriter, PrintWriter}
import java.util.concurrent.atomic.AtomicInteger

import checkers.Game._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object App extends App {

  val store: ListBuffer[ScoredState] = ListBuffer[ScoredState]()

  def transformToEploitable(scoredState: ScoredState): ScoredState = {
    val theCurrentPlayer = Player.nextPlayer(state.nextPlayer)
    val opp = state.nextPlayer
    val transformedGrid = state.grid.map(line => line.map {
      case `theCurrentPlayer` => 1
      case `opp` => -1
      case _ => 0
    })
    ScoredState(state.copy(grid = transformedGrid), scoredState.score)
  }

  def monteCarloEvalFunctionWithStore(samples: Int)(state: State, player: Player, count: AtomicInteger): Double = {
    val score = monteCarloEvalFunction(samples)(state, player, count)
    store += ScoredState(state, score)
    score
  }

  val state: State = Game.newGame()

  val turnHandler: (Action, State, Double) => Unit = (action, currentState, score) => {
    println()
    println(s"Playing $action with expected winrate $score for player ${Player.nextPlayer(currentState.nextPlayer)}")
    println(currentState)
  }


  val file = new File("./out/dump.txt")
  file.getParentFile.mkdirs()
  val fw = new FileWriter(file, false)

  val states = playTillEndWithEvalFunction(state, monteCarloEvalFunctionWithStore(100), turnHandler)
  println(s"after ${states.size} turns")
  states.foreach { s =>
    println(s)
    println()
  }

  val endGameWinner: Option[String] = states.lastOption.flatMap(_.winner.map(_.toString))
  println(s"winner is ${endGameWinner.getOrElse("nobody")}")

  println(s"${store.size} samples")

  val pw = new PrintWriter(fw)

  try {
    store.map(transformToEploitable)
      .sortBy {
        _.score
      }
      .foreach { scoredState =>
        pw.println(scoredState.state.grid.map(_.mkString(" ")).mkString(" ") + " " + scoredState.score)
      }
  } finally {
    pw.close()
  }
}
