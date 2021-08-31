package wunderreise

import org.scalacheck.Gen
import org.scalatest.concurrent.{Signaler, TimeLimitedTests}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.Span
import org.scalatest.time.SpanSugar._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.annotation.nowarn


class SingleTrainSpec extends AnyFunSpec with Matchers with ScalaCheckPropertyChecks
  with TimeLimitedTests with TestUtil {

  override def timeLimit: Span = 5.seconds
  //noinspection ScalaDeprecation
  @nowarn
  override val defaultTestSignaler: Signaler = (testThread: Thread) => testThread.stop()



  describe("without requests") {
    it("does not move") {
      forAll(trains, Gen.choose(0,100)) { (train,steps) =>
        train.isIdle shouldBe true
        train.after(steps).isIdle shouldBe true
        train.after(steps).position shouldEqual train.position
      }
    }
    it("counts time") {
      forAll(trains, Gen.choose(0,100)) { (train,steps) =>
        train.after(steps).time shouldEqual steps
      }
    }
  }

  describe("with one request") {
    it("comes to pickup") {
      forAll(trains, terminals , terminals){ (tr, from, to) =>
        tr.assign(from->to).whenDoneOrAt(from).position shouldBe from
      }
    }
    it("comes to drop-off after pickup") {
      forAll(trains, terminals , terminals){ (tr, from, to) =>
        val atStart = tr.assign(from->to)
        val atPickup = atStart.whenDoneOrAt(from)
        val atDropOff = atPickup.whenDoneOrAt(to)
        atDropOff.position shouldBe to
        atDropOff.time should be >= atPickup.time
      }
    }
  }

  describe("with some particular requests ") {
    it("does not loop endlessly"){
      val t = Train(16).assign(16 -> 7).assign(16 -> 8).assign(16,17)
              .next.next
              .assign(12->10).assign(12->13).assign(22->16)
      noException should be thrownBy t.whenDone
    }


    describe("serves optimally") {
      it("6->7, 5->4; starting at 4") {
        route(Train(4).assign(6 -> 7).assign(5 -> 4)) shouldEqual
          List(4, 5, 4, 5, 6, 7)
      }

      it("11->12, 7->9; starting at 10") {
        route(Train(10).assign(11 -> 12).assign(7 -> 9)) shouldEqual
          List(10, 11, 12, 11, 10, 9, 8, 7, 8, 9)
      }

      it("13->20,12->8; starting at 10") {
        route(Train(10).assign(13 -> 20).assign(12 -> 8)) shouldEqual
          List(10, 11, 12, 11, 10, 9, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
      }

      it("6->1,12->0; starting at 4") {
        route(Train(4).assign(6 -> 1).assign(12 -> 0)) shouldEqual
          List(4, 5, 6, 7, 8, 9, 10, 11, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)

      }

      it("6->0, 12->2; starting at 4") {
        route(Train(4).assign(6 -> 0).assign(12 -> 2)) shouldEqual
          List(4, 5, 6, 7, 8, 9, 10, 11, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
      }

      it("20->21, 26->20, 19->20; starting at 21 ") {
        route(Train(21).assign(20 -> 21).assign(26 -> 20).assign(19 -> 20)) shouldEqual
          List(21, 22, 23, 24, 25, 26, 25, 24, 23, 22, 21, 20, 19, 20, 21)
      }

      it("11->4, 5->1, 2->10; starting at 6") {
        route(Train(6).assign(11 -> 4).assign(5 -> 1).assign(2 -> 10)) shouldEqual
          List(6, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 10, 9, 8, 7, 6, 5, 4)
      }
    }
  }

  describe("with many random requests") {
    it("is eventually done") {
      forAll(trains, Gen.listOf(pickups)) { (tr, pickups) =>
        val booked = pickups.foldLeft(tr)(_ assign _)
        noException should be thrownBy booked.whenDone
      }
    }
    it("always comes to drop-off after pickup") {
      forAll(trains, Gen.listOf(pickups)) { (tr, pickups) =>
        val booked = pickups.foldLeft(tr)(_ assign _)
        for((from,to)<-pickups){
          booked.whenDoneOrAt(from).whenDoneOrAt(to).position shouldBe to
        }
      }
    }
  }
}
