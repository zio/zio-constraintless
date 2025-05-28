package zio.constraintless.examples

import zio.constraintless._

trait Expr[As <: TypeList, A] {
  def /(
      expr: Expr[As, A]
  )(implicit ev: A `IsElementOf` As, ev2: Int `IsElementOf` As): Expr[As, Int] =
    Ratio(this, expr, ev, ev2)
}

case class Value[As <: TypeList, A](a: A, e: A `IsElementOf` As)
    extends Expr[As, A]

case class Prod[As <: TypeList, A, B](
    a: Expr[As, A],
    b: Expr[As, B],
    c1: A `IsElementOf` As,
    c2: B `IsElementOf` As
) extends Expr[As, (A, B)]

case class Cond[As <: TypeList, A](
    expr: Expr[As, Boolean],
    ifCond: Expr[As, A],
    thenCond: Expr[As, A],
    c1: A `IsElementOf` As,
    c2: Boolean `IsElementOf` As
) extends Expr[As, A]

case class Ratio[As <: TypeList, A](
    l: Expr[As, A],
    r: Expr[As, A],
    c1: A `IsElementOf` As,
    c2: Int `IsElementOf` As
) extends Expr[As, Int]

case class EqE[As <: TypeList, A](
    l: Expr[As, A],
    r: Expr[As, A],
    c1: Eq[A],
    c2: A `IsElementOf` As,
    c3: Boolean `IsElementOf` As
) extends Expr[As, Boolean]

object Expr {
  def value[A, As <: TypeList](a: A)(implicit
      e: A `IsElementOf` As
  ): Expr[As, A] =
    Value(a, e)

  def prod[A, B, C, As <: TypeList](a: Expr[As, A], b: Expr[As, B])(implicit
      e: A `IsElementOf` As,
      f: B `IsElementOf` As
  ): Expr[As, (A, B)] =
    Prod(a, b, e, f)

  def condE[A, As <: TypeList](
      l: Expr[As, Boolean],
      exec1: Expr[As, A],
      exec2: Expr[As, A]
  )(implicit
      e: `IsElementOf`[A, As],
      b: `IsElementOf`[Boolean, As]
  ): Expr[As, A] =
    Cond(l, exec1, exec2, e, b)

  def eqE[A, As <: TypeList](exec1: Expr[As, A], exec2: Expr[As, A])(implicit
      e: `IsElementOf`[A, As],
      b: `IsElementOf`[Boolean, As],
      eq: Eq[A]
  ): Expr[As, Boolean] =
    EqE(exec1, exec2, eq, e, b)
}

trait Eq[A] {
  def eq(l: A, r: A): A
}

object compiler {

  type IO[A] = Option[A]

  def compile[As <: TypeList, A](
      expr: Expr[As, A]
  )(implicit ev: Instances[IntBool, As]): IO[A] =
    expr match {
      case Cond(expr, ifCond, thenCond, c1, c2) =>
        compile(expr).flatMap({ bool =>
          if (bool) compile(ifCond) else compile(thenCond)
        })
      case EqE(l, r, c1, c2, c3) => Some(compile(l) == compile(r))
      case Prod(a, b, c1, c2) =>
        compile(a).flatMap(aa => compile(b).map(bb => (aa, bb)))

      case Ratio(a, b, c, d) =>
        compile(a).flatMap(aa =>
          compile(b).map(bb => {
            def showInt[B](b: B)(use: IntBool[B]): Int =
              use.toInt(b)

            ev.withInstance(showInt(aa))(c) / ev.withInstance(
              showInt(bb)
            )(c)
          })
        )

      case Value(a, e) => Some(a)
    }

  // Making use of `Instances` in paper rather than `AllIntBool`
  def compileSM[As <: TypeList, A](
      expr: Expr[As, A]
  )(implicit ev: Instances[IntBool, As]): String =
    expr match {
      case Cond(expr, ifCond, thenCond, c1, c2) =>
        s"if (${compileSM(expr)} then ${compileSM(ifCond)} else ${compileSM(thenCond)}} "
      case EqE(l, r, c1, c2, c3) => s" ${compileSM(l)} Equals ${compileSM(r)}"

      case Prod(a, b, c1, c2) =>
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
      case Value(a, constraint) =>
        def showInt[B](b: B)(ev: IntBool[B]): String =
          s"${ev.toInt(b)}"
        s"${ev.withInstance(showInt(a))(constraint)}"

    }

  def pretty[As <: TypeList, A](
      expr: Expr[As, A]
  )(implicit show: Instances[Show, As]): String = {
    expr match {
      case Cond(expr, ifCond, thenCond, c1, c2) =>
        val x = pretty(expr)
        val y = pretty(ifCond)
        val z = pretty(thenCond)
        s"${x} ${y} ${z}"

      case Prod(a, b, c1, c2) =>
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

      case Value(a, constraint) =>
        def showInt[B](b: B)(ev: Show[B]): String =
          s"${ev.show(b)}"

        s"${show.withInstance(showInt(a))(constraint)}"
    }
  }

}

object ExprExample extends App {
  import TypeList._

  // These types will act as the types that the entire program structure supports
  type AllowedTypes = Int :: Double :: Boolean :: (Double, Double) :: End

  // Every tree is part of the allowed type
  val value: Expr[AllowedTypes, Double] =
    Expr.condE[Double, AllowedTypes](
      Expr.value(true),
      Expr.value(1.0),
      Expr.value(0.0)
    )

  val value2: Expr[AllowedTypes, (Double, Double)] =
    Expr.prod(
      Expr.value[Double, AllowedTypes](1.0),
      Expr.value[Double, AllowedTypes](2.0)
    )

  val value3 = value2 / value2

  import Instances._
  println(compiler.compileSM(value))
  println(compiler.pretty(value))
  println(compiler.pretty(value2))
  println(compiler.compileSM(value2))
  println(compiler.compile(value3))
  println(compiler.pretty(value3))
}
