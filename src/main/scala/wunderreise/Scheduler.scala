package wunderreise
import scala.annotation.tailrec
import scala.collection.immutable._



case class Scheduler(trains: IndexedSeq[Train], unassigned: Set[Pickup] = Set.empty) {
  def pickup(p:Pickup):Scheduler = copy(unassigned = unassigned + p)

  type PickupEstimate = (Pickup,Train,Time)

  def bestEstimate(p:Pickup): PickupEstimate = trains.map( t => (p, t, t.assign(p).whenDone.time) )
                                                     .minBy(_._3)
  private
  def assign(t:Train, p: Pickup) = copy( trains  = trains.updated(trains.indexOf(t), t.assign(p)),
                                         unassigned = unassigned - p )
  @tailrec private
  def rescheduleR:Scheduler = {
    if(unassigned.isEmpty) this else {
      val (worstPickup, bestTrainForWorstPickup, _) = unassigned.map(bestEstimate).maxBy(_._3)
      assign(bestTrainForWorstPickup, worstPickup).rescheduleR
    }
  }

  def reschedule: Scheduler = {
    val allUnassigned = copy( trains = trains.map(_.copy(pickups = SortedSet.empty)),
                              unassigned = unassigned ++ trains.flatMap(_.pickups) )
    allUnassigned.rescheduleR
  }

  def moveTrains: Scheduler = copy(trains = trains.map(_.next))

  def next: Scheduler = reschedule.moveTrains

  def isIdle: Boolean = trains.forall(_.isIdle) && unassigned.isEmpty

  def whenDone:Scheduler = if(isIdle) this else next.whenDone

}

object Scheduler {
  def apply(trains:Train*):Scheduler = apply(trains.toIndexedSeq)
}
