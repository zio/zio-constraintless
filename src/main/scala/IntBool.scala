trait IntBool[A] {
  def toInt(a: A): Int
}

object IntBool {
  implicit def tuple[A](implicit ev: IntBool[A]): IntBool[(A, A)] =
    new IntBool[(A, A)] {
      override def toInt(a: (A, A)): Int =
        ev.toInt(a._1) + ev.toInt(a._2)
    }

  implicit val evInt: IntBool[Int] =
    (a: Int) => a

  implicit val evDouble: IntBool[Double] =
    (a: Double) => a.toInt

  implicit val evBool: IntBool[Boolean] = {
    case true  => 1
    case false => 0
  }
}
