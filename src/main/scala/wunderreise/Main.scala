package wunderreise

  object Main {
    def dirSymbol(dir:Direction): String = dir match {
      case Right => ">"
      case Left => "<"
      case _ => "-"
    }

    def main(args: Array[String]): Unit = {
      if( args.length>0 && args.forall(_.forall(_.isDigit)))  {
        val trains = args.map(_.toInt).map(Train.apply).toIndexedSeq
        val pair = """(\d+)\s+(\d+)""".r
        io.Source.stdin.getLines().foldLeft(Scheduler(trains)) { (sch, input) =>
          val next =  if (input.isEmpty) sch.next else {
            val pickups = input.split("[;,]").map(_.trim).map { case pair(from, to) => from.toInt -> to.toInt }
            sch.pickup(pickups: _*).next
          }
          println(next.trains.map(t => "%3d %1s ".format(t.position,dirSymbol(t.direction))).mkString("|"))
          next
        }
        println("Please provide starting positions for the trains as parameters.\n" +
                "Nothing else is accepted")
        System.exit(1)

      }


    }

}
