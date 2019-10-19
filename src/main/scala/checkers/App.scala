package checkers

import checkers.Game.playSeveralTurnsRandomly

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
  val endState = playSeveralTurnsRandomly(state, turns)
  println(s"after $turns turns")
  println(endState)
}

trait Greeting {
  lazy val greeting: String = "hello"
}
