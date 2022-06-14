// From paper 3. Higher-order constraints

trait All[C, As <: HList] {
  def withElem[B, D](as: Proxy[As], trap: All.Trap[C, B] => D)(implicit ev: B Elem As): D
}


object All {
  import HList._

  /**
   * """
   * We transform the constraint c b into a data type which ‘traps’ the relationship between c and b:
   * """
   */
  final case class Trap[C, B](ev: C, ev2: B)

  implicit def allHList[C, A, As <: HList](implicit c: C, a: A, ev: All[C, As]): All[C, A :: As ] = new All[C, A :: As] {
    override def withElem[B, D](as: Proxy[A :: As], trap: Trap[C, B] => D)(implicit ev2: Elem[B, A :: As]): D =
      ev2.evidence match {
        case evidence: Evidence[B, A :: As] => evidence match {
          case Head() => trap(Trap(c, a.asInstanceOf[B]))
          case Tail(x) => ev.withElem(Proxy[As], trap)(x)
        }
      }

  }
}