package wunderreise

import org.scalacheck.Gen

trait TestUtil {
  val terminals = Gen.choose(0,32)
  val trains = for(start <- terminals) yield Train(start)
  val pickups = for (from <- terminals; to <- terminals) yield (from, to)

  def route(t: Train): Seq[Terminal] =
    LazyList.iterate(t)(_.next).takeWhile(!_.isIdle).toList.map(_.position)

  def routes(s:Scheduler): Seq[Seq[Terminal]] =
    LazyList.iterate(s)(_.next).takeWhile(!_.isIdle).toList.map(_.trains.map(_.position)).transpose

}
