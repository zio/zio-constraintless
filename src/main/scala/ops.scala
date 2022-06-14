// Any application's execution planner can be represented by a simple trait
// However the tricky bit that we are experimenting is the input to your application
// is an existential type - a type member represnted by Inner(example: a query)

import executor._

sealed trait Ops[F[_], +O] { self =>

    def zip[O1 >: O](that: Ops[F, O1])(implicit O: MathOp[(O1, O1)]): Ops[F, (O1, O1)] = 
        Ops.Zip(self, that, O)

    def ratio[O1 >: O](that: Ops[F, O1])(implicit O: MathOp[O1]): Ops[F, O1] = 
        Ops.Ratio(self, that, O)    
}



object Ops {

    final case class Pure[F[_], A](x: F[A], M: CanExecute[F, A], O: MathOp[A]) extends Ops[F, A]

    final case class Zip[F[_],  B](left: Ops[F, B], right: Ops[F,  B],  O: MathOp[(B, B)]) extends Ops[F,  (B, B)]

    final case class Ratio[F[_], B](left: Ops[F,  B], right: Ops[F,  B], O: MathOp[B]) extends Ops[F, B]

    def pure[F[_], A](f: F[A])(implicit C: CanExecute[F, A], O: MathOp[A]) = 
        Pure(f, C, O)        

}