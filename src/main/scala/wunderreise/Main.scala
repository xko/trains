package wunderreise

import scala.io.Source.stdin

object Main {

    def dirSymbol(dir:Direction): String = dir match {
      case Right => ">"
      case Left => "<"
      case _ => "-"
    }

    def tap(scheduler: Scheduler):Scheduler = {
      println(scheduler.trains.map(t => "%3d %1s ".format(t.position, dirSymbol(t.direction))).mkString("|"))
      scheduler
    }

    def main(args: Array[String]): Unit = {
      if( args.length>0 && args.forall(_.forall(_.isDigit)))  {
        val trains = args.map(_.toInt).map(Train.apply).toIndexedSeq
        val pair = """(\d+)\s+(\d+)""".r
        val booked = stdin.getLines().foldLeft( tap(Scheduler(trains:_*)) ) { (sch, input) =>
          tap( if (input.isEmpty) sch.next else {
            val pickups = input.split("[;,]").map(_.trim).toIndexedSeq.map {
              case pair(from, to) => from.toInt -> to.toInt
            }
            sch.pickup(pickups: _*).next
          } )
        }
        Iterator.iterate(booked)(_.next).takeWhile(!_.isIdle).foreach(tap)
      } else {
        System.err.println("Please provide starting positions for the trains as parameters.\n" +
                           "Nothing else is accepted")
        System.exit(1)
      }


    }

  }
