// http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf

object Paper {

  trait Eq[A] {
    def eq(l: A, r: A): A
  }

  trait Expr[A]

  case class ValueE[A](a: A) extends Expr[A]
  case class CondE[A](expr: Expr[Boolean], ifCond: Expr[A], thenCond: Expr[A])
      extends Expr[A]
  case class EqE[A](l: Expr[A], r: Expr[A], constraint: Eq[A])
      extends Expr[Boolean]

  def compileSM[A](expr: Expr[A]): String = ???

  trait Evidence[A, As <: HList]

  import HList._

  case class Head[A, As <: HList]() extends Evidence[A, A :: As]

  case class Tail[A, B, As <: HList]() extends Evidence[A, B :: As]

  trait Elem[A, As <: HList] {
    def evidence: Evidence[A, As]
  }
}
