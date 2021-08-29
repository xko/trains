package wunderreise

import scala.collection.immutable._
import scala.math._

case class Train(position: Terminal, dropoffs: SortedSet[Terminal], pickups: Set[Pickup], time: Time = 0 ) {

  lazy val direction: Direction = if(isIdle) Idle else {
    val left  = dropoffs.rangeUntil(position) ++
                pickups.collect{ case from->to if from < position && to >= from => from } ++
                pickups.collect{ case from->to if from < position && to <  from => to }
    val right = dropoffs.rangeFrom(position) ++
                pickups.collect{ case from->to if from >= position && to < from => from } ++
                pickups.collect{ case from->to if from >= position && to>= from => to }
    if(left.isEmpty) Right
    else if( right.isEmpty) Left
    else if( abs(left.min-position) < abs(right.max-position) ) Left
    else Right
  }

  def assign(pickup: Pickup): Train = copy(pickups = pickups + pickup)

  val isIdle: Boolean = dropoffs.isEmpty && pickups.isEmpty

  def board: Train = {
    val(boarding,remaining) = pickups.partition(_._1 == position)
    copy(dropoffs = dropoffs ++ boarding.map(_._2) - position, pickups = remaining )
  }

  def move: Train = if(isIdle) copy(time = time + 1)
                    else copy(position = position + direction, time = time +1)

  def next: Train = board.move

  def after(steps:Int):Train = if(steps == 0) this else next.after(steps -1)

  def whenDoneOrAt(t: Terminal):Train = if(isIdle || position == t) this else next.whenDoneOrAt(t)

  def whenDone: Train = if(isIdle) this else next.whenDone

}

object Train {
  def apply(pos: Terminal): Train = Train(pos, SortedSet.empty[Int], Set.empty )
}
