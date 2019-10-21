package checkers

import checkers.Game._
import MathsUtils._

object App extends App {
  val state: State = Game.newGame()
  println(state)
  println()
  val actions = Game.findAllActions(state)
  println(s"${actions.size} actions:")
  println(actions.map{ case (action, _) => action}.mkString("\n"))
  println()

  println(Grid.printableGrids(actions.map {
    case (_, resState) => resState.grid
  }.toSeq))
  println()

  val states = playTillEndWithEvalFunction(state, basicEvalFunction)
  println(s"after ${states.size} turns")
  states.foreach { s =>
    println(s)
    println()
  }

  val endGameWinner: Option[String] = states.lastOption.flatMap(_.winner.map(_.toString))
  println(s"winner is ${endGameWinner.getOrElse("nobody")}")

  val allSizes = (0 until 100) map {iGame =>
    val state: State = Game.newGame()
    val history = playTillEndWithEvalFunction(state, basicEvalFunction)
    history.size
  }

  val min = allSizes.min
  val avg = average(allSizes)
  val max = allSizes.max

  println(s"Game stats over ${allSizes.size} samples: $min - $avg - $max")
}
