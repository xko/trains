package wunderreise

import org.scalacheck.Gen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class SingleTrainSpec extends AnyFunSpec with Matchers with ScalaCheckPropertyChecks with TestUtil {

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
        tr.assign((from, to)).whenDoneOrAt(from).position shouldBe from
      }
    }
    it("comes to drop-off after pickup") {
      forAll(trains, terminals , terminals){ (tr, from, to) =>
        val atStart = tr.assign((from, to))
        val atPickup = atStart.whenDoneOrAt(from)
        val atDropOff = atPickup.whenDoneOrAt(to)
        atDropOff.position shouldBe to
        atDropOff.time should be >= atPickup.time
      }
    }
  }

  describe("with some particular requests") {
    it("starting at 4 serves 6->7,5->4") {
      route(Train(4).assign(6, 7).assign(5, 4)) shouldEqual Seq(4, 5, 6, 7, 6, 5, 4)
    }

    it("starting at 10 serves 11->12,7->9") {
      route(Train(10).assign(11, 12).assign(7, 9)) shouldEqual Seq(10, 11, 12, 11, 10, 9, 8, 7, 8, 9)
    }

    it("starting at 10 serves 13->20,12->8") {
      route(Train(10).assign(13, 20).assign(12, 8)) shouldEqual
        Seq(10, 11, 12, 11, 10, 9, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
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
