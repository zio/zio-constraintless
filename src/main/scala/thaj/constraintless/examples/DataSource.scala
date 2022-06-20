package thaj.constraintless.examples

sealed trait DataSource[A]

object DataSource {
  case class Aurora[A](logic: String) extends DataSource[A]
  case class FlatSchema[A](metricName: String) extends DataSource[A]

  def aurora[A](logic: String): DataSource[A] =
    Aurora(logic)

  def flatschema[A](metricName: String): DataSource[A] =
    FlatSchema(metricName)
}
