trait IntBool[A] {
  def toInt(a: A): Int
}

object IntBool {
  implicit val evInt: IntBool[Int] =
    (a: Int) => a

  implicit val evDouble: IntBool[Double] =
    (a: Double) => a.toInt

  implicit val evBool: IntBool[Boolean] = {
    case true  => 1
    case false => 0
  }
}
