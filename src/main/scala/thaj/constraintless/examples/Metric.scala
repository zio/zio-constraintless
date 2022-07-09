package thaj.constraintless.examples

import java.awt.Window
import java.time.Duration

sealed trait Metric[A] {
  def orElse(that: Metric[A]): Metric[A] =
    Metric.OrElse(this, that)
}

object Metric {

  def aurora(logic: String): Metric[Double] =
    Source(DataSource.aurora(logic), ???, ???)

  def flatSchema(metricName: String): Metric[Double] =
    Source(DataSource.flatschema(metricName), ???, ???)

  final case class Source[A](
      dataSource: DataSource[A],
      from: RelativeTime,
      to: RelativeTime
  ) extends Metric[A]

  final case class Union[A](m1: List[Metric[A]]) extends Metric[A]
  final case class OrElse[A](m1: Metric[A], m2: Metric[A]) extends Metric[A]

}
