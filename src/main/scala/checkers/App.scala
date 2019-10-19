package checkers

object App extends Greeting with App {
  val state: State = Game.newGame()
  println(state)
  println()
  val actions = Game.findAllActions(state)
  println(s"${actions.size} actions:")
  println(actions.map{ case (action, _) => action}.mkString("\n"))
  println()

  actions.foreach {
    case (_, resState) =>
      println(resState)
      println()
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
