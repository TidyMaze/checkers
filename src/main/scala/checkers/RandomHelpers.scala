package checkers

import scala.util.{Failure, Random, Success, Try}

object RandomHelpers {
  val random = new Random()

  def randomIn[A](arr: Seq[A]): Try[A] =
    if(arr.isEmpty) Failure(new RuntimeException("empty array"))
    else Success(arr(random.nextInt(arr.size)))
}
