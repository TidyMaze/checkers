package checkers

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Random, Success, Try}

object RandomHelpers {
  val random = new Random()

  def randomIn[A](arr: Seq[A]): Try[A] =
    if(arr.isEmpty) Failure(new RuntimeException("empty array"))
    else Success(arr(random.nextInt(arr.size)))

  def randPct(): Double = random.nextDouble()

  def randomIterator[A](xs: ArrayBuffer[A]): Iterator[A] = new RandomIterator[A](xs, random)
}

class RandomIterator[A](xs: ArrayBuffer[A], random: Random) extends Iterator[A] {
  private  val remainings = xs.indices.to[mutable.HashSet]

  override def hasNext: Boolean = remainings.nonEmpty

  override def next(): A = {
    val picked =  remainings.iterator.drop(random.nextInt(remainings.size)).next()
    remainings.remove(picked)
    xs(picked)
  }
}