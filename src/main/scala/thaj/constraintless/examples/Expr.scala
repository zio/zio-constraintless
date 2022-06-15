// http://www.doc.ic.ac.uk/~wlj05/files/Deconstraining.pdf
package thaj.constraintless.examples

import thaj.constraintless._

trait Expr[As <: HList, A] {
  def /(
      expr: Expr[As, A]
  )(implicit ev: A Elem As, ev2: Int Elem As): Expr[As, Int] =
    Ratio(this, expr, ev, ev2)
}

case class ValueE[As <: HList, A](a: A, e: A Elem As) extends Expr[As, A]

case class ProdE[As <: HList, A, B](
    a: Expr[As, A],
    b: Expr[As, B],
    c1: A Elem As,
    c2: B Elem As
) extends Expr[As, (A, B)]

case class CondE[As <: HList, A](
    expr: Expr[As, Boolean],
    ifCond: Expr[As, A],
    thenCond: Expr[As, A],
    c1: A Elem As,
    c2: Boolean Elem As
) extends Expr[As, A]

case class Ratio[As <: HList, A](
    l: Expr[As, A],
    r: Expr[As, A],
    c1: A Elem As,
    c2: Int Elem As
) extends Expr[As, Int]

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

  def prodE[A, B, C, As <: HList](a: Expr[As, A], b: Expr[As, B])(implicit
      e: A Elem As,
      f: B Elem As
  ): Expr[As, (A, B)] =
    ProdE(a, b, e, f)

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

  type IO[A] = Option[A]

  def compile[As <: HList, A](
      expr: Expr[As, A]
  )(implicit ev: All[IntBool, As]): IO[A] =
    expr match {
      case CondE(expr, ifCond, thenCond, c1, c2) =>
        compile(expr).flatMap({ bool =>
          if (bool) compile(ifCond) else compile(thenCond)
        })
      case EqE(l, r, c1, c2, c3) => Some(compile(l) == compile(r))
      case ProdE(a, b, c1, c2) =>
        compile(a).flatMap(aa => compile(b).map(bb => (aa, bb)))

      case Ratio(a, b, c, d) =>
        compile(a).flatMap(aa =>
          compile(b).map(bb => {
            def showInt[B](b: B)(trap: All.Trap[IntBool, B]): Int =
              trap.ev.toInt(b)

            ev.withElem(Proxy[As])(showInt(aa))(c) / ev.withElem(Proxy[As])(
              showInt(bb)
            )(c)
          })
        )

      case ValueE(a, e) => Some(a)
    }

  // Making use of `All` in paper rather than `AllIntBool`
  def compileSM[As <: HList, A](
      expr: Expr[As, A]
  )(implicit ev: All[IntBool, As]): String =
    expr match {
      case CondE(expr, ifCond, thenCond, c1, c2) =>
        s"if (${compileSM(expr)} then ${compileSM(ifCond)} else ${compileSM(thenCond)}} "
      case EqE(l, r, c1, c2, c3) => s" ${compileSM(l)} Equals ${compileSM(r)}"

      case ProdE(a, b, c1, c2) =>
        s" ${compileSM(a)} zipped with ${compileSM(b)}"

      case Ratio(a, b, c2, _) =>
        val x = compileSM(a)
        val y = compileSM(b)
        s"${x} / ${y}"

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
        def showInt[B](b: B)(trap: All.Trap[IntBool, B]): String =
          s"${trap.ev.toInt(b)}"
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

      case ProdE(a, b, c1, c2) =>
        val x = pretty(a)
        val y = pretty(b)
        s"${x} ${y}"

      case Ratio(a, b, c2, _) =>
        val x = pretty(a)
        val y = pretty(b)
        s"${x} / ${y}"

      case EqE(l, r, c1, c2, c3) =>
        val y = pretty(l)
        val z = pretty(r)
        s"${y} ${z}"

      case ValueE(a, constraint) =>
        def showInt[B](b: B)(trap: All.Trap[Show, B]): String =
          s"${trap.ev.show(b)}"

        s"${show.withElem(Proxy[As])(showInt(a))(constraint)}"
    }
  }

}

object ExprExample extends App {
  import HList._

  // These types will act as the types that the entire program structure supports
  type AllowedTypes = Int :: Double :: Boolean :: (Double, Double) :: HNil

  // Every tree is part of the allowed type
  val value: Expr[AllowedTypes, Double] =
    Expr.condE[Double, AllowedTypes](
      Expr.valueE(true),
      Expr.valueE(1.0),
      Expr.valueE(0.0)
    )

  val value2: Expr[AllowedTypes, (Double, Double)] =
    Expr.prodE(
      Expr.valueE[Double, AllowedTypes](1.0),
      Expr.valueE[Double, AllowedTypes](2.0)
    )

  val value3 = value2 / value2

  import All._
  println(compiler.compileSM(value))
  println(compiler.pretty(value))
  println(compiler.pretty(value2))
  println(compiler.compileSM(value2))
  println(compiler.compile(value3))
  println(compiler.pretty(value3))
}
