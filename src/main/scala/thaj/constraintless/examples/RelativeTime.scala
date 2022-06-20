package thaj.constraintless.examples

import java.time.Duration

sealed trait RelativeTime {
  def minus(duration: Duration): RelativeTime =
    RelativeTime.Minus(duration)
}

object RelativeTime {
  case class Now() extends RelativeTime
  case class Minus(duration: Duration) extends RelativeTime

  def now(): RelativeTime =
    Now()

}
