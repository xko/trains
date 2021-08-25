package wunderreise

import scala.collection.immutable._



case class Train(position: Terminal, schedule: SortedSet[Terminal], queue: Set[Request], time: Long = 0 ) {
  val direction: Direction = schedule.ordering.compare(1, -1).sign

  implicit class InMyDirection(t: Terminal) {
    def before(other: Terminal): Boolean = schedule.ordering.compare(t, other) <= 0
  }

  def assign(request: Request): Train = request match {
    case (from,to) if position.before(from) && from.before(to) => copy(schedule = schedule + from + to)
    case (from,_)  if position.before(from) => copy(schedule = schedule + from, queue = queue + request )
    case _ => copy(queue = queue + request)
  }

  val isIdle: Boolean = schedule.isEmpty && queue.isEmpty

  def turn: Train = queue.foldLeft {
    copy(schedule = SortedSet.empty(schedule.ordering.reverse), queue = Set.empty)
  } ( _ assign _ )

  def next: Train = {
    if(isIdle) copy(time = time + 1)
    else if (schedule.nonEmpty) copy(position + direction, schedule - position, queue, time + 1)
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