// Make this run without compile time or runtime exceptions !
object MakeThisWork extends App {

  sealed trait Expr[A] {
    def zip[B](that: Expr[B]): Expr[(A, B)] =
      Zip(this, that)

    def plus(that: Expr[A]): Expr[A] =
      Sum(this, that)
  }

  final case class IntExpr(value: Int) extends Expr[Int]
  final case class StringExpr(value: String) extends Expr[String]
  final case class Sum[A](left: Expr[A], right: Expr[A]) extends Expr[A]
  final case class Zip[A, B](left: Expr[A], right: Expr[B]) extends Expr[(A, B)]

  def int(value: Int): Expr[Int] =
    IntExpr(value)

  def string(value: String): Expr[String] =
    StringExpr(value)

  val addExpr: Expr[Int] =
    int(1).plus(int(2))

  val zippedExpr: Expr[(Int, Int)] =
    addExpr.zip(addExpr)

  // A =:= (a, b)

  def run[A](expr: Expr[A])(f: (A, A) => A): A = {
    expr match {
      case IntExpr(value)    => value
      case StringExpr(value) => value
      case Sum(left, right) =>
        val left_ = run(left)(f)
        val right_ = run(right)(f)
        println(left_)
        println(right_)

        f(left_, right_)

      case z: Zip[a, b] =>
        val left_ = run(z.left.asInstanceOf[Expr[Any]])(f.asInstanceOf[(Any, Any) => Any])
        val right_ = run(z.right.asInstanceOf[Expr[Any]])(f.asInstanceOf[(Any, Any) => Any])

        (left_, right_).asInstanceOf[A]
    }
  }

  val add: ((Int, Int), (Int, Int)) => (Int, Int) =
    (a, b) => (a._1 + b._1, (a._2 + b._2))

  println(run(zippedExpr)(add)) // doesn't work


  val zipped = int(1).zip(int(2))
  val addZipped  = zipped.plus(zipped)

  run(addZipped)(add) // does work
}
