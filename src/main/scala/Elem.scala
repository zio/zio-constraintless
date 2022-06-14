trait Evidence[A, As <: HList]

import HList._

final case class Head[A, As <: HList]() extends Evidence[A, A :: As]

final case class Tail[A, B, As <: HList](ev: Elem[A, As]) extends Evidence[A, B :: As]

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


object ex2 {
  import HList._

  Elem[Double, String :: Int :: Double :: HNil] // compiles
  // Elem[Double, String :: Int :: HNil] doesn't compile


}