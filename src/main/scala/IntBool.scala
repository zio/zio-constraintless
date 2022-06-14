trait IntBool[A] {
  def toInt(a: A): Int
}

object IntBool {
  implicit val evInt: IntBool[Int] =new IntBool[Int] {
    override def toInt(a: Int): Int = a
  }

  implicit val evBool: IntBool[Boolean] = new IntBool[Boolean] {
    override def toInt(a: Boolean): Int = ???
  }
}