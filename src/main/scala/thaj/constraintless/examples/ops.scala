package thaj.constraints.examples

import Ops.Pure
import Ops.Zip
import thaj.constraintless._

/** Ops is an exec planner whose leaf node is a higher kinded type
  */
sealed trait Ops[As <: HList, F[_], O] { self =>

  def <*>[B](
      that: Ops[As, F, B]
  )(implicit elem1: O Elem As, elem2: B Elem As): Ops[As, F, (O, B)] =
    Ops.Zip(self, that, elem1, elem2)

  def /(that: Ops[As, F, O])(implicit elem: O Elem As): Ops[As, F, O] =
    Ops.Ratio(self, that, elem)
}

object Ops {

  final case class Pure[As <: HList, F[_], A](fa: F[A], constraint: A Elem As)
      extends Ops[As, F, A]

  final case class Zip[As <: HList, F[_], A, B](
      left: Ops[As, F, A],
      right: Ops[As, F, B],
      elem1: A Elem As,
      elem2: B Elem As
  ) extends Ops[As, F, (A, B)]

  final case class Ratio[As <: HList, F[_], B](
      left: Ops[As, F, B],
      right: Ops[As, F, B],
      elem1: B Elem As
  ) extends Ops[As, F, B]

}

object queryplanner {
  import HList._

  // My IO is query that produces key and value
  sealed trait Query[K, V] {
    def map[B](f: V => B): Query[K, B] =
      DMap(this, f)
  }

  case class Api[K, V](f: String => (K, V)) extends Query[K, V]
  // DMap is the direct mapping over a query output
  // while Ops.Map could be mapping over the result of multiple query operations
  case class DMap[K, A, B](self: Query[K, A], f: A => B) extends Query[K, B]

  type ExecPlan[As <: HList, K, A] = Ops[As, Query[K, *], A]

  object ExecPlan {
    def pure[As <: HList, K, A](query: Query[K, A])(implicit
        ev: A Elem As
    ): ExecPlan[As, K, A] =
      Ops.Pure[As, Query[K, *], A](query, ev)
  }

}

object queryplannercompiler {
  import queryplanner._

  type IO[K, A] = Map[K, A]

  type InnerResult[As <: HList, K, A] =
    Either[ExecSummary[As, K, A], Query[K, A]]

  // With new Ops the exec summary will be as follows
  final case class ExecSummary[As <: HList, K, A](
      value: IO[K, A],
      detail: Ops[As, InnerResult[As, K, *], A]
  )

  // You can see the hidden information in this exec plan
  def run[As <: HList, K, A](
      plan: ExecPlan[As, K, A],
      f: Query[K, *] ~> Map[K, *]
  )(implicit mathOp: All[MathOp, As]): IO[K, A] = {
    def loop[B](plan: ExecPlan[As, K, B]): IO[K, B] = {
      plan match {
        case pure: Pure[As, Query[K, *], B] =>
          f(pure.fa) // Here is the key problem that we are facing.

        case zip: Zip[As, Query[K, *], b, c] =>
          val map1 = loop(zip.left)
          val map2 = loop(zip.right)
          map1
            .map({ case (k, b) =>
              map2
                .get(k)
                .map({ c =>
                  (k, (b, c))
                })
                .getOrElse(
                  (
                    k,
                    (b, mathOp.withElem[c, c](Proxy[As])(_.ev.zero)(zip.elem2))
                  )
                )
            })
            .toMap

        case ratio: Ops.Ratio[As, Query[K, *], B] =>
          val map1 = loop(ratio.left)
          val map2 = loop(ratio.right)

          def zero[B](trap: All.Trap[MathOp, B]): B =
            trap.ev.zero

          map1.map({ case (k, b) =>
            map2
              .get(k)
              .map({ b2 =>
                (
                  k,
                  mathOp.withElem[B, B](Proxy[As])(trap =>
                    trap.ev.ratio(b, b2)
                  )(ratio.elem1)
                )
              })
              .getOrElse(
                (
                  k,
                  mathOp.withElem[B, B](Proxy[As])(trap => trap.ev.zero)(
                    ratio.elem1
                  )
                )
              )
          })

      }
    }

    loop(plan)
  }

}

object QueryPlannerSpec extends App {
  import queryplanner._
  import HList._

  type PlannerTypes = Int :: (Int, Int) :: HNil

  val v1: ExecPlan[PlannerTypes, String, Int] =
    ExecPlan.pure[PlannerTypes, String, Int](
      Api[String, Int](_ => ("US", 20))
    )

  val v2: ExecPlan[PlannerTypes, String, Int] =
    ExecPlan.pure[PlannerTypes, String, Int](
      Api[String, Int](_ => ("US", 10))
    )

  def myPlan: ExecPlan[PlannerTypes, String, Int] =
    v1 / v2

  // One way to get a tuple where the original query was (k, a) but program later produced (k, (a, a))
  val zippedPlan: ExecPlan[PlannerTypes, String, (Int, Int)] =
    myPlan <*> myPlan

  // This implies your query implementor need to know about how to get a (k, (a, a)) given a request
  // Ideally this make sense, because that leads to a scalable code base, where our values from data-source are bound to change
  // in future rather than sticking on to a specific value
  // However this scenairo implies, any Query[K, A] => Map[K, A], and A can take any shape ==> f: Query[K, *] ~> Map[K, *]
  def zippedPlan_ : ExecPlan[PlannerTypes, String, (Int, Int)] =
    ExecPlan.pure(Api[String, (Int, Int)](str => ("US", (1, 1))))

  def queryCompiler = new (Query[String, *] ~> Map[String, *]) {
    override def apply[A](a: Query[String, A]): Map[String, A] =
      a match {
        case Api(f) =>
          val response: String = ""
          val kv = f(response)
          Map(kv)

        case dmap: DMap[String, a, b] =>
          apply(dmap.self).map({ case (string, a) =>
            (string, dmap.f(a))
          })
      }
  }

  val result = queryplannercompiler.run(zippedPlan, queryCompiler)

  println(result)
}
