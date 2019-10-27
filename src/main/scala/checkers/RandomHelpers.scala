package checkers

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Random, Success, Try}

object RandomHelpers {
  val random = new Random()

  def randomIn[A](arr: Seq[A]): Try[A] =
    if(arr.isEmpty) Failure(new RuntimeException("empty array"))
    else Success(arr(random.nextInt(arr.size)))

  def randPct(): Double = random.nextDouble()

  def shuffle[A](xs: ArrayBuffer[A]): Unit = {
    def swap(i1: Int, i2: Int) {
      val tmp = xs(i1)
      xs(i1) = xs(i2)
      xs(i2) = tmp
    }

    for (n <- xs.length to 2 by -1) {
      val k = random.nextInt(n)
      swap(n - 1, k)
    }
  }

}
