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

  def compileSM[As <: HList, A](
      expr: Expr[As, A]
  )(implicit ev: AllIntBool[As]): String =
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
        s"${ev.toInt(Proxy[As], a)(constraint)}"

    }

}

import HList.{::, _}

/** All the type that comes arbitrarily in the tree has an instance of IntBool -
  * AllIntBool.
  *
  * The only requirement is Any B can be converted to Int, as far as B is an
  * element of the As (in particular Proxy[As])
  */
trait AllIntBool[As <: HList] {
  def toInt[B](p: Proxy[As], b: B)(implicit ev: B Elem As): Int
}

object AllIntBool {
  implicit def instanceOfHList[A, As <: HList](implicit
      ev: IntBool[A],
      all: AllIntBool[As]
  ): AllIntBool[A :: As] =
    new AllIntBool[A :: As] {
      override def toInt[B](p: Proxy[A :: As], b: B)(implicit
          elem: Elem[B, A :: As]
      ): Int = elem.evidence match {
        case evidence: Evidence[B, A :: As] =>
          evidence match {
            // head which was A is definitely B, a super safe casting
            case Head() => ev.toInt(b.asInstanceOf[A])
            case e @ Tail() =>
              all.toInt(Proxy[As], b)(e.ev)
          }

      }

    }

  implicit val hlist: AllIntBool[HNil] =
    new AllIntBool[HNil] {
      override def toInt[B](p: Proxy[HNil], b: B)(implicit
          ev: Elem[B, HNil]
      ): Int =
        sys.error("hello")
    }
}

object ex3 extends App {
  import HList._

  // These types will act as the types that the entire program structure supports
  type AllowedTypes = Int :: (Double :: Boolean :: HNil)

  // Every tree is part of the allowed type
  val value =
    Expr.condE[Double, AllowedTypes](
      Expr.valueE(true),
      Expr.valueE(1.0),
      Expr.valueE(0.0)
    )

  println(compiler.compileSM(value))
}
