package wunderreise
import scala.annotation.tailrec
import scala.collection.immutable._



case class Scheduler(trains: IndexedSeq[Train], unassigned: Set[Pickup] = Set.empty) {
  def pickup(p:Pickup):Scheduler = copy(unassigned = unassigned + p)

  private
  def reschedule: Scheduler = {
    type PickupEstimate = (Pickup,Train,Time)
    def bestEstimate(trains: Seq[Train])(p:Pickup): PickupEstimate =
      trains.map( t => (p, t, t.assign(p).whenDone.time) ).minBy(_._3)

    @tailrec
    def reassign(trains: IndexedSeq[Train], pickups: Set[Pickup]): IndexedSeq[Train] =
      if (pickups.isEmpty) trains else {
        val (bestPickup, bestTrain, _) = pickups.map(bestEstimate(trains)).minBy(_._3)
        reassign( trains.updated(trains.indexOf(bestTrain), bestTrain.assign(bestPickup)),
                  pickups - bestPickup )
      }

    Scheduler(reassign( trains.map(_.copy(pickups = SortedSet.empty)),
                        unassigned ++ trains.flatMap(_.pickups) ))
  }

  private
  def moveTrains: Scheduler = copy(trains = trains.map(_.next))

  def next: Scheduler = reschedule.moveTrains

  def isIdle: Boolean = trains.forall(_.isIdle) && unassigned.isEmpty

  def whenDone:Scheduler = if(isIdle) this else next.whenDone

}

object Scheduler {
  def apply(trains:Train*):Scheduler = apply(trains.toIndexedSeq)
}
