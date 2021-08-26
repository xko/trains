package wunderreise

import scala.collection.immutable._



case class Train(position: Terminal, dropoffs: SortedSet[Terminal], pickups: Set[Request], time: Long = 0 ) {
  val direction: Direction = dropoffs.ordering.compare(1, -1).sign

  val ahead: SortedSet[Terminal] = (dropoffs ++ pickups.map(_._1)).rangeFrom(position)

  def assign(pickup: Request): Train = copy(pickups = pickups + pickup)

  val isIdle: Boolean = dropoffs.isEmpty && pickups.isEmpty

  def turn: Train = copy(dropoffs = SortedSet.empty(dropoffs.ordering.reverse) ++ dropoffs)

  def board: Train = {
    val(boarding,remaining) = pickups.partition(_._1 == position)
    copy(dropoffs = dropoffs ++ boarding.map(_._2) - position, pickups = remaining )
  }

  def move: Train = copy(position = position + direction, time = time +1)

  def next: Train = {
    if(isIdle) copy(time = time + 1)
    else if (ahead.nonEmpty) board.move
    else turn.next
  }

  def after(steps:Int):Train = if(steps == 0) this else next.after(steps -1)

  def isAt(t: Terminal): Boolean = position == t

  def whenDoneOrAt(t: Terminal):Train = if(isIdle || isAt(t)) this else{
    next.whenDoneOrAt(t)
  }

}

object Train {
  def apply(pos: Terminal): Train = Train(pos, SortedSet.empty[Int], Set.empty )
}