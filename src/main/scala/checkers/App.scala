package checkers

import checkers.Game._

object App extends App {
  val state: State = Game.newGame()

  val turnHandler: (Action, State, Double) => Unit = (action, currentState, score) => {
    println()
    println(s"Playing $action with expected winrate $score for player ${Player.nextPlayer(currentState.nextPlayer)}")
    println(currentState)
  }

  val states = playTillEndWithEvalFunction(state, monteCarloEvalFunction(500), turnHandler)
  println(s"after ${states.size} turns")
  states.foreach { s =>
    println(s)
    println()
  }

  val endGameWinner: Option[String] = states.lastOption.flatMap(_.winner.map(_.toString))
  println(s"winner is ${endGameWinner.getOrElse("nobody")}")

}
