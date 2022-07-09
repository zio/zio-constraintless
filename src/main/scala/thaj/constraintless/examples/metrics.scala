package thaj.constraintless.examples

object metricslist {
  val m1 =
    Metric.aurora("count(*).from(Table)")

  val m2 =
    Metric.flatSchema("hello")

  val playBackAttemptsCount: Metric[Double] =
    m1.orElse(m2)
}
