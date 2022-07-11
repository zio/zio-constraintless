// From paper 3. Higher-order constraints

package thaj.constraintless

trait All[C[_], As <: HList] {
  // trap is name inspired from the paper trapping the relationship between C[_] and B
  def withElem[B, D](trap: C[B] => D)(implicit
      ev: B Elem As
  ): D
}

object All {
  import HList._

  implicit def allHList[C[_], A, As <: HList](implicit
      c: C[A],
      ev: All[C, As]
  ): All[C, A :: As] = new All[C, A :: As] {
    override def withElem[B, D](
        trap: C[B] => D
    )(implicit ev2: Elem[B, A :: As]): D =
      ev2.evidence match {
        case evidence: Evidence[B, A :: As] =>
          evidence match {
            case Head() =>
              trap(
                c.asInstanceOf[C[B]]
              ) // Coz we have compile time evidence that B is infact A
            case Tail(x) => ev.withElem(trap)(x)
          }
      }
  }

  // The definition is slightly from what mentioned in the paper where it traverses hlist
  implicit def allHList2[C[_]]: All[C, HNil] = new All[C, HNil] {
    override def withElem[B, D](trap: C[B] => D)(implicit
        ev: Elem[B, HNil]
    ): D =
      sys.error("hmmm")
  }
}
