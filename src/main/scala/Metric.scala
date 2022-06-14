trait Metric[+A] {
    def /[A1 >: A] (that: Metric[A1]) = 
        Metric.Ratio(this, that)

    def <*>[A1 >: A] (that: Metric[A1])(implicit M: executor.MathOp[A1]) = 
        Metric.Zip(this, that, implicitly[executor.MathOp[(A1, A1)]])  
}

object Metric {
    final case class Source[A](s: Query[A], c: executor.CanExecute[Query, A], m: executor.MathOp[A]) extends Metric[A]
    final case class Ratio[A](left: Metric[A], right: Metric[A]) extends Metric[A]
    final case class Zip[A](left: Metric[A], right: Metric[A], m: executor.MathOp[(A, A)]) extends Metric[(A, A)]


    val numerator: Metric[Double] = 
        Source(Query.Api("a metric from an external api"), ???, ???)

    val denominator: Metric[Double] = 
        Source(Query.Api("another metric from an external api"), ???, ???)   

    val ratio = numerator / denominator  

    val execPlan = planner.plan(ratio.<*>(numerator))
    val result: executor.IO[(Double, Double)] = executor.executor(execPlan)  
}