package wunderreise

import scala.annotation.tailrec
import scala.collection.immutable._
import scala.math._

case class Train(position: Terminal, dropoffs: SortedSet[Terminal], pickups: Set[Pickup], time: Time = 0 ) {

  lazy val direction: Direction = if(isIdle) Idle else {
    val (togoRight, togoLeft) = ( pickups ++ dropoffs.map((position,_)) )
                                .partition{ case from->to => to > from }

    val combinedRight = togoRight.foldLeft(SortedSet.empty[Pickup])
    { case (acc, nFrom->nTo) => acc.lastOption match {
      case Some(pFrom->pTo) if (pFrom to pTo).contains(nFrom) => acc - acc.last + (pFrom->max(pTo, nTo))
      case _ => acc + (nFrom->nTo)
    } }
    val combinedLeft = togoLeft.foldRight(SortedSet.empty[Pickup])
    { case (nFrom->nTo, acc) => acc.headOption match {
      case Some(pFrom->pTo) if (pTo to pFrom).contains(nFrom) => acc - acc.head + (pFrom->min(pTo, nTo))
      case _ => acc + (nFrom->nTo)
    } }

    val leftStops  = combinedRight.collect{ case from->to if from <  position => from } ++
                     combinedLeft.collect { case from->to if from <= position => to }
    val rightStops = combinedRight.collect{ case from->to if from >= position => to } ++
                     combinedLeft.collect { case from->to if from >  position => from }

    if (leftStops.isEmpty) Right else if(rightStops.isEmpty) Left
    else if( abs(leftStops.min-position) < abs(rightStops.max-position) ) Left
    else Right
  }

  def assign(pickup: Pickup): Train = copy(pickups = pickups + pickup)

  val isIdle: Boolean = dropoffs.isEmpty && pickups.isEmpty


  val boarding: Set[Pickup] = pickups.filter(_._1 == position)

  private
  def board: Train = {
    copy(dropoffs = dropoffs ++ boarding.map(_._2) - position, pickups = pickups -- boarding )
  }

  private
  def move: Train = if(isIdle) copy(time = time + 1)
                    else copy(position = position + direction, time = time +1)

  def next: Train = board.move

  @tailrec final
  def after(steps:Int):Train = if(steps == 0) this else next.after(steps -1)

  @tailrec final
  def whenDoneOrAt(t: Terminal):Train = if(isIdle || position == t) this else next.whenDoneOrAt(t)

  @tailrec final
  def whenDone: Train = if(isIdle) this else next.whenDone

}

object Train {
  def apply(pos: Terminal): Train = Train(pos, SortedSet.empty[Int], SortedSet.empty  )
}
