package thaj.constraintless

// Useful mostly when your GADT has a legitimate higher kinded type, that is not part of a Hughe's scheme
// Example: case class Runner[F[_], A](value: F[A]) extends Program[F, A]
// If higher kinded type `F` needs to be run as part of the compiler, 99% you need a natural transformation
// to encode the quantification
trait ~>[F[_], G[_]] {
  def apply[A](a: F[A]): G[A]
}
