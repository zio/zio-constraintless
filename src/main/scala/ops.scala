/** Ops is an exec planner whose leaf node is a higher kinded type
  * @tparam F
  * @tparam O
  */
//sealed trait Ops[As, F[_], +O] { self =>
//
//  def zip[O1 >: O](that: Ops[As, F, O1]): Ops[As, F, (O1, O1)] =
//    Ops.Zip(self, that)
//
//  def ratio[O1 >: O](that: Ops[F, O1]): Ops[F, O1] =
//    Ops.Ratio(self, that)
//}
//
//object Ops {
//
//  final case class Pure[As <: HList, F[_], A](x: F[A]) extends Ops[As, F, A]
//
//  final case class Zip[As <: HList, F[_], B](
//      left: Ops[As, F, B],
//      right: Ops[As, F, B]
//  ) extends Ops[As, F, (B, B)]
//
//  final case class Ratio[As <: HList, F[_], B](
//      left: Ops[As, F, B],
//      right: Ops[As, F, B]
//  ) extends Ops[As, F, B]
//
//  def pure[F[_], A](f: F[A]) =
//    Pure(f)
//
//}
