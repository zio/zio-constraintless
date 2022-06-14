import Ops.Zip 
import Ops.Pure
import Ops.Ratio

object executor {

    trait CanExecute[-F[_], A] {
      def execute(f: F[A]): IO[A]
    }

    // Only for explanation
    type IO[A] = Option[A]

    trait MathOp[A] {
        def /[A1 >: A] (a: A1, b: A1): A1
    }

    object MathOp {
        def apply[A](implicit ev: MathOp[A]): MathOp[A] = ev

        implicit def math[A: MathOp] = new MathOp[(A, A)] {
          override def /[A1 >: (A, A)](a: A1, b: A1): (A, A) = ???

        }

        implicit def doubleMathOp: MathOp[Double] = ???

    }

    // A planner can be converted to Input/Ouput tree
    def executor[F[_], A: MathOp, B](ops: Ops[F, A]): IO[A] = {  

        def loop[B](ops: Ops[F, B]): IO[B] =
            ops match {
              case Pure(x, m, o) => m.execute(x)
              case Ratio(left, right, m) => loop(left).flatMap(b => loop(right).map(b1 => m./(b, b1)))
              case z:Zip[f, b] => loop(z.left).flatMap(b => loop(z.right).map(c => (b, c)))
            }

        loop(ops)
    }
 
}