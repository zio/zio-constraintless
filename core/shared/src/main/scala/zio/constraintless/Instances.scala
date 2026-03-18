package zio.constraintless

import `IsElementOf`._

trait Instances[TypeClass[_], As <: TypeList] {
  def withInstance[B, D](use: TypeClass[B] => D)(implicit
      ev: B `IsElementOf` As
  ): D
}

object Instances {
  import TypeList._

  implicit def instancesCons[C[_], A, As <: TypeList](implicit
      c: C[A],
      ev: Instances[C, As]
  ): Instances[C, A :: As] = new Instances[C, A :: As] {
    override def withInstance[B, D](
        use: C[B] => D
    )(implicit ev2: `IsElementOf`[B, A :: As]): D =
      ev2 match {
        case Head() =>
          use(
            c.asInstanceOf[C[B]]
          ) // We have compile-time evidence that B is in fact A
        case Tail(x) => ev.withInstance(use)(x)
      }

  }

  // The definition is slightly different from what is mentioned in the paper where it traverses hlist
  implicit def instancesEnd[C[_]]: Instances[C, End] = new Instances[C, End] {
    override def withInstance[B, D](use: C[B] => D)(implicit
        ev: `IsElementOf`[B, End]
    ): D =
      sys.error("unreachable")
  }
}
