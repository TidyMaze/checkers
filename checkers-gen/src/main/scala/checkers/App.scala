package checkers

import java.io.{File, FileWriter, PrintWriter}
import java.util.concurrent.atomic.AtomicInteger

import checkers.Game._
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

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

  def getNNScore(state: State) = {
    val linear = state.grid.flatten.map(_.toDouble)
    val input = Nd4j.create(linear.toArray, 1, 64)
    model.output(input, false).getDouble(0L)
  }

  def neuralNetworkEvalFunction(state: State, player: Player, count: AtomicInteger): Double = {
    val usableState = transformToEploitable(state)
    getNNScore(usableState)
  }

  val modelLocation = new File("../out/model.zip")
  val model = MultiLayerNetwork.load(modelLocation, true)

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
      val states = playTillEndWithEvalFunction(Game.newGame(), neuralNetworkEvalFunction, turnHandler)

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
