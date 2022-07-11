package thaj.constraintless

trait Lift[F[_]] {
  def lift[A](a: A): F[A]
}

object Lift {
  def apply[F[_]](implicit ev: Lift[F]): Lift[F] = ev

  implicit def liftF[E]: Lift[Either[E, *]] = new Lift[Either[E, *]] {
    override def lift[A](a: A): Either[E, A] = Right(a)
  }
}
