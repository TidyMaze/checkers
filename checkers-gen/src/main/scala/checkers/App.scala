package checkers

import java.io.{File, FileWriter, PrintWriter}
import java.util.concurrent.atomic.AtomicInteger

import checkers.Game._

import scala.collection.mutable.ListBuffer

object App extends App {

  val store: ListBuffer[ScoredState] = ListBuffer[ScoredState]()

  def transformToEploitable(scoredState: ScoredState): ScoredState = {
    val theCurrentPlayer = Player.nextPlayer(scoredState.state.nextPlayer)
    val opp = scoredState.state.nextPlayer
    val transformedGrid = scoredState.state.grid.map(line => line.map {
      case `theCurrentPlayer` => 1
      case `opp` => -1
      case _ => 0
    })
    ScoredState(scoredState.state.copy(grid = transformedGrid), scoredState.score)
  }

  def monteCarloEvalFunctionWithStore(samples: Int)(state: State, player: Player, count: AtomicInteger): Double = {
    val score = monteCarloEvalFunction(samples)(state, player, count)
    store += ScoredState(state, score)
    score
  }

  val turnHandler: (Action, State, Double) => Unit = (action, currentState, score) => {
    println()
    println(s"Playing $action with expected winrate $score for player ${Player.nextPlayer(currentState.nextPlayer)}")
    println(currentState)
  }


  val file = new File("../out/dump.txt")
  file.getParentFile.mkdirs()
  val fw = new FileWriter(file, true)

  val pw = new PrintWriter(fw)
  try {

    (0 until 10).foreach { _ =>
      store.clear()
      val states = playTillEndWithEvalFunction(Game.newGame(), monteCarloEvalFunctionWithStore(200), turnHandler, store)

//      endGamePrint(states)

      println(s"${store.size} samples")

      store.map(transformToEploitable)
        .sortBy {
          _.score
        }
        .foreach { scoredState =>
          val linearGrid = scoredState.state.grid.flatten.map(_.toString)
          pw.println((linearGrid :+ scoredState.score.toString).mkString(","))
        }
    }
  } finally {
    pw.close()
  }

  private def endGamePrint(states: List[State]) = {
    println(s"after ${states.size} turns")
    states.foreach { s =>
      println(s)
      println()
    }

    val endGameWinner: Option[String] = states.lastOption.flatMap(_.winner.map(_.toString))
    println(s"winner is ${endGameWinner.getOrElse("nobody")}")
  }
}
