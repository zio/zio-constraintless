// http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf

trait Expr[As <: HList, A]

case class ValueE[As <: HList, A](a: A, e: A Elem As) extends Expr[As, A]
case class CondE[As <: HList, A](
    expr: Expr[As, Boolean],
    ifCond: Expr[As, A],
    thenCond: Expr[As, A],
    c1: A Elem As,
    c2: Boolean Elem As
) extends Expr[As, A]

case class EqE[As <: HList, A](
    l: Expr[As, A],
    r: Expr[As, A],
    c1: Eq[A],
    c2: A Elem As,
    c3: Boolean Elem As
) extends Expr[As, Boolean]

object Expr {
  def valueE[A, As <: HList](a: A)(implicit e: A Elem As): Expr[As, A] =
    ValueE(a, e)

  def condE[A, As <: HList](
      l: Expr[As, Boolean],
      exec1: Expr[As, A],
      exec2: Expr[As, A]
  )(implicit e: Elem[A, As], b: Elem[Boolean, As]): Expr[As, A] =
    CondE(l, exec1, exec2, e, b)

  def eqE[A, As <: HList](exec1: Expr[As, A], exec2: Expr[As, A])(implicit
      e: Elem[A, As],
      b: Elem[Boolean, As],
      eq: Eq[A]
  ): Expr[As, Boolean] =
    EqE(exec1, exec2, eq, e, b)
}

trait Eq[A] {
  def eq(l: A, r: A): A
}

object compiler {

  // Making use of `All` in paper rather than `AllIntBool`
  def compileSM[As <: HList, A](
      expr: Expr[As, A]
  )(implicit ev: All[IntBool, As]): String =
    expr match {
      case CondE(expr, ifCond, thenCond, c1, c2) =>
        s"if (${compileSM(expr)} then ${compileSM(ifCond)} else ${compileSM(thenCond)}} "
      case EqE(l, r, c1, c2, c3) => s" ${compileSM(l)} Equals ${compileSM(r)}"

      /** From paper:
        *
        * """ From the definition of Exp above, all we know about the type of x
        * (b, say) is that b ∈ as. For this particular implementation we need to
        * establish the fact that b is also either an integer or a boolean, i.e.
        * we need to satisfy the constraint IntBool b before we can invoke
        * toInt.
        *
        * """
        */
      case ValueE(a, constraint) =>
        def showInt[B](b: B)(trap: All.Trap[IntBool, B]): String = s"${trap.ev.toInt(b)}"
        s"${ev.withElem(Proxy[As])(showInt(a))(constraint)}"

    }

  def pretty[As <: HList, A](
      expr: Expr[As, A]
  )(implicit show: All[Show, As]): String = {
    expr match {
      case CondE(expr, ifCond, thenCond, c1, c2) =>
        val x = pretty(expr)
        val y = pretty(ifCond)
        val z = pretty(thenCond)
        s"${x} ${y} ${z}"

      case EqE(l, r, c1, c2, c3) =>
        val y = pretty(l)
        val z = pretty(r)
        s"${y} ${z}"

      case ValueE(a, constraint) =>
        def showInt[B](b: B)(trap: All.Trap[Show, B]): String = s"${trap.ev.show(b)}"

        s"${show.withElem(Proxy[As])(showInt(a))(constraint)}"
    }
  }

}

object ex3 extends App {
  import HList._

  // These types will act as the types that the entire program structure supports
  type AllowedTypes = Int :: (Double :: Boolean :: HNil)

  // Every tree is part of the allowed type
  val value: Expr[AllowedTypes, Double] =
    Expr.condE[Double, AllowedTypes](
      Expr.valueE(true),
      Expr.valueE(1.0),
      Expr.valueE(0.0)
    )

  import All._
  println(compiler.compileSM(value))
  println(compiler.pretty(value))
}
