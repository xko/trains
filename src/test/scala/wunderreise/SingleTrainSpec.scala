package wunderreise

import org.scalacheck.Gen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class SingleTrainSpec extends AnyFunSpec with Matchers with ScalaCheckPropertyChecks {
  val terminals = Gen.choose(0,32)
  val trains = for(start <- terminals) yield Train(start)

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
        tr.assign((from, to)).whenDoneOrAt(from).isAt(from) shouldBe true
      }
    }
    it("comes to drop-off after pickup") {
      forAll(trains, terminals , terminals){ (tr, from, to) =>
        val atStart = tr.assign((from, to))
        val atPickup = atStart.whenDoneOrAt(from)
        val atDropOff = atPickup.whenDoneOrAt(to)
        atDropOff.isAt(to) shouldBe true
        atDropOff.time should be >= atPickup.time
      }
    }
  }

  describe("with many requests") {
    //TODO
  }


}