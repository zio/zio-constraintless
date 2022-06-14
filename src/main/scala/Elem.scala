import HList._

trait Elem[A, As <: HList] {
  def evidence: Evidence[A, As]
}

object Elem {
  def apply[A, As <: HList](implicit ev: Elem[A, As]): Elem[A, As] = ev

  implicit def elemA[A, As <: HList]: Elem[A, A :: As] = new Elem[A, A :: As] {
    def evidence: Evidence[A, A :: As] = Head[A, As]()
  }

  implicit def elemeB[A, B, As <: HList](implicit
      ev: Elem[A, As]
  ): Elem[A, B :: As] = new Elem[A, B :: As] {
    override def evidence: Evidence[A, B :: As] = Tail[A, B, As](ev)
  }
}
