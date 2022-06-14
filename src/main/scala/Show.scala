trait Show[A] {
  def show(a: A): String
}

object Show {
  implicit val showDouble: Show[Double] =
    (a: Double) => a.toString

  implicit val showBoolean: Show[Boolean] =
    (a: Boolean) => a.toString

  implicit val showInt: Show[Int] =
    (a: Int) => a.toString
}
