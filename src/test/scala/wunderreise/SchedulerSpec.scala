package wunderreise

import org.scalacheck.Gen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.collection.immutable.SortedSet

class SchedulerSpec extends AnyFunSpec with Matchers with ScalaCheckPropertyChecks with TestUtil {
  describe("with random trains and requests"){
    import org.scalacheck.Gen._

    it("is eventually done"){
      forAll(listOfN(32,trains), listOf(pickups)){ (trains, pickups) => whenever(trains.nonEmpty) {
        val booked = Scheduler(trains:_*).pickup(pickups:_*)
        noException should be thrownBy booked.whenDone
      } }
    }

    def allBoarded(booked: Scheduler) = for {
      state->step <- Iterator.iterate(booked)(_.next).takeWhile(!_.isIdle).zipWithIndex
      train <- state.trains
      boarding <- train.boarding
    } yield step->boarding

    it("boards all requested"){
      forAll(listOfN(32, trains), listOf(pickups)){ (trains, pickups) => whenever(trains.nonEmpty) {
        val booked = Scheduler(trains:_*).pickup(pickups:_*)
        allBoarded(booked).map(_._2).toSet shouldEqual pickups.toSet
      } }
    }

    def allDelivered(booked: Scheduler) = for {
      state -> step <- Iterator.iterate(booked)(_.next).takeWhile(!_.isIdle).zipWithIndex
      fromStep -> (from -> to) <- allBoarded(booked)
      train <- state.trains
      delivered = from -> to if train.position == to && fromStep <= step
    } yield delivered

    it("delivers all boarded") {
      forAll(listOfN(32, trains), listOf(pickups)) { (trains, pickups) => whenever(trains.nonEmpty) {
          val booked = Scheduler(trains: _*).pickup(pickups: _*)
          allDelivered(booked).toSet shouldEqual pickups.toSet
      } }
    }

    describe("with pickups requested in the process") {
      it("is eventually done") {
        forAll( listOfN(32, trains), listOf(choose(0, 20)), listOf(listOfN(7,pickups)) ) { (trains, stops, pickups) =>
          whenever(trains.nonEmpty) {
            val booked = stops.sorted.zip(pickups).foldLeft( Scheduler(trains: _*) ){ case (sch, stop->pickups) =>
              sch.after(stop).pickup(pickups:_*)
            }
            noException should be thrownBy booked.whenDone
          }
        }
      }
    }
  }

  describe("with particular trains/requests"){
    it("sends 2 trains in opposite directions"){
      val s = Scheduler(Train(9),Train(10)).pickup(2->4,3->0,5->2)
                                           .pickup(13->20,12->8,2->2)
      routes(s) shouldEqual List( List(9,  8, 7, 6, 5, 4, 3, 2, 3, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0),
                                  List(10,11,12,11,10, 9, 8, 9,10,11,12,13,14,15,16,17,18,19,20) )
    }

    it("keeps 3 trains with local requests") {
      val s = Scheduler(Train(2),Train(10),Train(21)).pickup(2->4,3->0,5->2)
                                                     .pickup(13->11,12->8,9->10)
                                                     .pickup(20->21,26->20,19->20)
      routes(s) shouldEqual List( List( 2, 3, 4, 5, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0),
                                  List(10, 9,10,11,12,13,12,11,10, 9, 8, 8, 8, 8, 8, 8),
                                  List(21,20,19,20,21,22,23,24,25,26,25,24,23,22,21,20) )
    }
  }

}
