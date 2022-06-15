import Ops.Pure
import Ops.Zip

/** Ops is an exec planner whose leaf node is a higher kinded type
  */
sealed trait Ops[As <: HList, F[_], O] { self =>

  def zip[B](that: Ops[As, F, B]): Ops[As, F, (O, B)] =
    Ops.Zip(self, that)

  def /(that: Ops[As, F, O]): Ops[As, F, O] =
    Ops.Ratio(self, that)
}

object Ops {

  final case class Pure[As <: HList, F[_], A](fa: F[A]) extends Ops[As, F, A]

  final case class Zip[As <: HList, F[_], A, B](
      left: Ops[As, F, A],
      right: Ops[As, F, B]
  ) extends Ops[As, F, (A, B)]

  final case class Ratio[As <: HList, F[_], B](
      left: Ops[As, F, B],
      right: Ops[As, F, B]
  ) extends Ops[As, F, B]

  def pure[F[_], A](f: F[A]) =
    Pure(f)

}

object queryplanner {
  import HList._

  // My IO is query that produces key and value
  sealed trait DataSource[A]

  case class Api[A]() extends DataSource[A]

  type Query[K, A] = DataSource[(K, A)]

  type ExecPlan[As <: HList, K, A] = Ops[As, Query[K, *], A]

  object ExecPlan {
    def pure[As <: HList, K, A](query: Query[K, A]): ExecPlan[As, K, A] =
      Ops.Pure[As, Query[K, *], A](query)
  }

}

object queryplannercompiler {
  import queryplanner._

  type IO[K, A] = Map[K, A]

  // You can see the hidden information in this exec plan
  def run[As <: HList, K, A](plan: ExecPlan[As, K, A]): IO[K, A] = {
    def loop[B](plan: ExecPlan[As, K, B]): IO[K, B] = {
      plan match {
        case pure: Pure[As, Query[K, *], B] => ???
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
                .getOrElse((k, (b, 0.asInstanceOf[c])))
            })
            .toMap

        case ratio: Ops.Ratio[As, Query[K, *], B] =>
          val map1 = loop(ratio.left)
          val map2 = loop(ratio.right)

          map1.map({ case (k, b) =>
            map2
              .get(k)
              .map({ b2 =>
                (
                  k,
                  (b.asInstanceOf[Int] / b2.asInstanceOf[Int]).asInstanceOf[B]
                )
              })
              .getOrElse((k, 0.asInstanceOf[B]))
          })

      }
    }

    loop(plan)
  }

}

object QueryPlannerSpec extends App {
  import queryplanner._

  def myPlan[As <: HList]: ExecPlan[As, String, Int] =
    ExecPlan.pure(Api[(String, Int)]()) / ExecPlan.pure(Api[(String, Int)]())

  // One way to get a tuple where the original query was (k, a) but program later produced (k, (a, a))
  def zippedPlan[As <: HList]: ExecPlan[As, String, (Int, Int)] =
    myPlan.zip(myPlan)

  // This implies your query implementor need to know about how to get a (k, (a, a)) given a request
  // Ideally this make sense, because that leads to a scalable code base, where our values from data-source are bound to change
  // in future rather than sticking on to a specific value
  // However this scenairo implies, any Query[K, A] => Map[K, A], and A can take any shape ==> f: Query[K, *] ~> Map[K, *]
  def zippedPlan_[As <: HList]: ExecPlan[As, String, (Int, Int)] =
    ExecPlan.pure(Api[(String, (Int, Int))])

  val result = queryplannercompiler.run(zippedPlan)

  println(result)
}
