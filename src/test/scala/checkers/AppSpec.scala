package checkers

import org.scalatest._

class AppSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    App.greeting shouldEqual "hello"
  }
}
