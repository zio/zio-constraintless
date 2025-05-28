package zio.constraintless

import TypeList._

sealed trait `IsElementOf`[Type, Types <: TypeList]

object `IsElementOf` {
  def apply[A, As <: TypeList](implicit
      ev: IsElementOf[A, As]
  ): `IsElementOf`[A, As] = ev

  final case class Head[A, As <: TypeList]() extends IsElementOf[A, A :: As]

  final case class Tail[A, B, As <: TypeList](ev: IsElementOf[A, As])
      extends `IsElementOf`[A, B :: As]

  implicit def IsElementOfHead[A, As <: TypeList]: IsElementOf[A, A :: As] =
    Head[A, As]()

  implicit def IsElementOfTail[A, B, As <: TypeList](implicit
      ev: `IsElementOf`[A, As]
  ): `IsElementOf`[A, B :: As] =
    Tail[A, B, As](ev)
}
