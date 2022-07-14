package zio.constraintless

import IsElementOf._

trait All[C[_], As <: TypeList] {
  // trap is name inspired from the paper trapping the relationship between C[_] and B
  def withElem[B, D](trap: C[B] => D)(implicit
      ev: B IsElementOf As
  ): D
}

object All {
  import TypeList._

  implicit def allCons[C[_], A, As <: TypeList](implicit
      c: C[A],
      ev: All[C, As]
  ): All[C, A :: As] = new All[C, A :: As] {
    override def withElem[B, D](
        trap: C[B] => D
    )(implicit ev2: IsElementOf[B, A :: As]): D =
      ev2 match {
        case Head() =>
          trap(
            c.asInstanceOf[C[B]]
          ) // Coz we have compile time evidence that B is infact A
        case Tail(x) => ev.withElem(trap)(x)
      }

  }

  // The definition is slightly from what mentioned in the paper where it traverses hlist
  implicit def allEnd[C[_]]: All[C, End] = new All[C, End] {
    override def withElem[B, D](trap: C[B] => D)(implicit
        ev: IsElementOf[B, End]
    ): D =
      sys.error("hmmm")
  }
}
