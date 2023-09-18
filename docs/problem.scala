// Make this run without compile time errors or runtime exceptions !
// without using the library constraintless
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

  def run[A](expr: Expr[A])(f: (A, A) => A): A = {
    expr match {
      case IntExpr(value)    => value
      case StringExpr(value) => value
      case Sum(left, right) =>
        val left_ = run(left)(f)
        val right_ = run(right)(f)

        f(left_, right_)

      case z: Zip[a, b] =>
        val left_ =
          run(z.left.asInstanceOf[Expr[Any]])(f.asInstanceOf[(Any, Any) => Any])
        val right_ = run(z.right.asInstanceOf[Expr[Any]])(
          f.asInstanceOf[(Any, Any) => Any]
        )

        (left_, right_).asInstanceOf[A]
    }
  }

  val add: ((Int, Int), (Int, Int)) => (Int, Int) =
    (a, b) => (a._1 + b._1, (a._2 + b._2))

  val added: Expr[Int] =
    int(1).plus(int(2))

  val zipAdded: Expr[(Int, Int)] =
    added.zip(added)

  // Make this work
  println(run(zipAdded)(add)) // doesn't work

  // How does the following work?

  val zipped = int(1).zip(int(2))
  val addZipped = zipped.plus(zipped)

  run(addZipped)(add) // does work

  // Another problem:
  // Can you try and avoid having to summon manually how to add various tuples and still work with run method.
  // i.e, I should be able to simply do `run(addZipped)` instead of `run(addZipped)(add)`
  run(addZipped) // make it compile
  run(added)
  run(zipAdded)
  run(added.zip(added).zip(added).zip(added))

}
