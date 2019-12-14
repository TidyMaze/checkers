package checkers

import java.io.{File, FileWriter, PrintWriter}
import java.util.concurrent.atomic.AtomicInteger

import checkers.Game._

object App extends App {

  def transformToEploitable(state: State): State = {
    val theCurrentPlayer = Player.nextPlayer(state.nextPlayer)
    val opp = state.nextPlayer
    val transformedGrid = state.grid.map(line => line.map {
      case `theCurrentPlayer` => 1
      case `opp` => -1
      case _ => 0
    })
    state.copy(grid = transformedGrid)
  }

  def monteCarloEvalFunctionWithStore(samples: Int)(state: State, player: Player, count: AtomicInteger): Double = {
    val score = monteCarloEvalFunction(samples)(state, player, count)
    val usableState = ScoredState(transformToEploitable(state), score)
    val linearGrid = usableState.state.grid.flatten.map(_.toString)
    pw.println((linearGrid :+ usableState.score.toString).mkString(","))
    pw.flush()
    assert(!pw.checkError())
    score
  }

  def getNNScore(state: State) = 42

  def neuralNetworkEvalFunction(samples: Int)(state: State, player: Player, count: AtomicInteger): Double = {
    val usableState = transformToEploitable(state)
    getNNScore(usableState)
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
      val states = playTillEndWithEvalFunction(Game.newGame(), monteCarloEvalFunctionWithStore(100), turnHandler)

//      endGamePrint(states)
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
