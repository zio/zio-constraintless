package thaj.constraintless

import HList._

trait Evidence[A, As <: HList]

final case class Head[A, As <: HList]() extends Evidence[A, A :: As]

final case class Tail[A, B, As <: HList](ev: Elem[A, As])
    extends Evidence[A, B :: As]
