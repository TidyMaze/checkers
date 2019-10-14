package checkers

object App extends Greeting with App {
  println(Game.newGame())
}

trait Greeting {
  lazy val greeting: String = "hello"
}
