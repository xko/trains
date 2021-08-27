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
      val s = Scheduler(Train(9),Train(10)).pickup(2,4).pickup(3,0).pickup(5,2)
                                           .pickup(13,20).pickup(12,8).pickup(2,2)
      routes(s) shouldEqual Seq( Seq( 9,10,11,12,11,10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 1 ,2, 3, 4),
                                 Seq(10,11,12,13,14,15,16,17,18,19,20,20,20,20,20,20,20,20,20,20) )
      //   TODO: can do better:
      //                    Seq( Seq(9,  8, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4),
      //                         Seq(10,11,12,11, 9, 8, 9,10,11,12,13,14,15,16,17,18,19,20) )

    }
  }

}
