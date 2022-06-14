trait IntBool[A] {
  def toInt(a: A): Int
}

object IntBool {
  implicit val evInt: IntBool[Int] =new IntBool[Int] {
    override def toInt(a: Int): Int = a
  }

  implicit val evDouble: IntBool[Double] =new IntBool[Double] {
    override def toInt(a: Double): Int = a.toInt
  }

  implicit val evBool: IntBool[Boolean] = new IntBool[Boolean] {
    override def toInt(a: Boolean): Int = ???
  }
}