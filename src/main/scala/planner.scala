object planner {
    def plan[F[_], A](metric: Metric[A])(implicit ev: executor.MathOp[A]): Ops[Query, A] =  {
     def loop[B](metric: Metric[B]): Ops[Query, B] = 
     
     metric match {
        case s: Metric.Source[B] => Ops.pure[Query, B](s.s)(s.c, s.m)
        case Metric.Zip(left, right, m) => loop(left).zip(loop(right))(m)
     }

     loop(metric)
    }

}