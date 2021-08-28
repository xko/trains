
package object wunderreise {
  type Terminal = Int
  type Direction = Int
  val Right: Direction = 1
  val Left: Direction = -1
  val Idle: Direction = 0
  type Time = Long
  type Pickup = (Terminal, Terminal)

}
