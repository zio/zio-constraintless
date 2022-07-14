package zio.constraintless.examples

trait Show[A] {
  def show(a: A): String
}

object Show {
  implicit def showA[A: Show]: Show[(A, A)] =
    (a: (A, A)) => {
      val showA = implicitly[Show[A]]
      s"(${showA.show(a._1)}, ${showA.show(a._2)})"
    }

  implicit val showDouble: Show[Double] =
    (a: Double) => a.toString

  implicit val showBoolean: Show[Boolean] =
    (a: Boolean) => a.toString

  implicit val showInt: Show[Int] =
    (a: Int) => a.toString
}
