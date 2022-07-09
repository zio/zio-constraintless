package thaj.constraintless.examples

trait MathOp[A] {
  def ratio(l: A, r: A): A
  def zero: A
}

object MathOp {
  implicit val mathInt: MathOp[Int] = new MathOp[Int] {
    override def ratio(l: Int, r: Int): Int = l / r
    override def zero: Int = 0
  }

  implicit def mathTuple[A](implicit ev: MathOp[A]): MathOp[(A, A)] =
    new MathOp[(A, A)] {
      override def ratio(l: (A, A), r: (A, A)): (A, A) =
        (ev.ratio(l._1, r._1), ev.ratio(l._2, r._2))

      override def zero: (A, A) = (ev.zero, ev.zero)
    }
}
