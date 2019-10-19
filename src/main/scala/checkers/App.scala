package checkers

object App extends Greeting with App {
  val state: State = Game.newGame()
  println(state)
  println()
  val actions = Game.findAllActions(state)
  println(s"${actions.size} actions:")
  actions.foreach {
    case (action, resState) =>
      println(s"$action =>")
      println(resState)
      println()
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
