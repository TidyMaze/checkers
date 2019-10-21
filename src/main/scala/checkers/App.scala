package checkers

import checkers.Game._

object App extends Greeting with App {
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
}

trait Greeting {
  lazy val greeting: String = "hello"
}
