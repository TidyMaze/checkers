package checkers

object Hello extends Greeting with App {
  println(Game.newGame())
}

trait Greeting {
  lazy val greeting: String = "hello"
}
