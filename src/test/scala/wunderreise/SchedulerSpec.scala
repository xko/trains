package wunderreise

import org.scalacheck.Gen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SchedulerSpec extends AnyFunSpec with Matchers with ScalaCheckPropertyChecks with TestUtil {
  describe("with random trains and requests"){
    it("is eventually done"){
      forAll(Gen.nonEmptyListOf(trains), Gen.listOf(pickups)){ (trains,pickups) =>
        val booked = pickups.foldLeft(Scheduler(trains:_*))(_ pickup  _)
        noException should be thrownBy booked.whenDone
      }
    }
  }

  describe("with particular trains/requests"){
    it("sends 2 trains in opposite directions"){
      val s = Scheduler(Train(9),Train(10)).pickup(2->4).pickup(3->0).pickup(5->2)
                                           .pickup(13->20).pickup(12->8).pickup(2->2)
      routes(s) shouldEqual List( List(9,  8, 7, 6, 5, 4, 3, 2, 3, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0),
                                  List(10,11,12,11,10, 9, 8, 9,10,11,12,13,14,15,16,17,18,19,20) )
    }

    it("keeps 3 trains with local requests") {
      val s = Scheduler(Train(2),Train(10),Train(21)).pickup(2->4).pickup(3->0).pickup(5->2)
                                                     .pickup(13->11).pickup(12->8).pickup(9->10)
                                                     .pickup(20->21).pickup(26->20).pickup(19->20)
      routes(s) shouldEqual List( List( 2, 3, 4, 5, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0),
                                  List(10, 9,10,11,12,13,12,11,10, 9, 8, 8, 8, 8, 8),
                                  List(21,22,23,24,25,26,25,24,23,22,21,20,19,20,21) )
    }
  }

}
