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

  val turns = 10
  val states = playSeveralTurnsWithEvalFunction(state, turns, basicEvalFunction)
  println(s"after $turns turns")
  states.foreach { s =>
    println(s)
    println()
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
