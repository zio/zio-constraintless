package zio.constraintless

import TypeList._

/** Evidence that `Type` is an element of the list `Types`. The compiler derives
  * it for a type that is in the list, and the code does not compile for a type
  * that is not. `IsElementOf` is invariant, so `Type` must be one of the listed
  * types itself: a subtype or a supertype of an element does not count. When a
  * type appears more than once, the derived evidence points at its first
  * position.
  *
  * A value is either [[IsElementOf.Head]], for the first element, or
  * [[IsElementOf.Tail]], for an element of the rest of the list. It can be
  * written infix with the name in backticks, as in the example.
  *
  * {{{
  * import zio.constraintless.TypeList._
  *
  * type Allowed = Int :: Boolean :: End
  *
  * val first: Int `IsElementOf` Allowed      = IsElementOf[Int, Allowed]
  * val second: Boolean `IsElementOf` Allowed = IsElementOf[Boolean, Allowed]
  * // first is Head(), second is Tail(Head())
  * }}}
  *
  * @see
  *   [[https://zio.dev/zio-constraintless/ ZIO Constraintless]]
  */
sealed trait `IsElementOf`[Type, Types <: TypeList]

/** The cases of [[IsElementOf]] and the implicit methods that derive them.
  */
object `IsElementOf` {

  /** Returns the evidence that `A` is an element of `As`, which the compiler
    * derives. It does not compile when `A` is not in the list.
    */
  def apply[A, As <: TypeList](implicit
      ev: IsElementOf[A, As]
  ): `IsElementOf`[A, As] = ev

  /** Evidence that the type is the first element of the list.
    */
  final case class Head[A, As <: TypeList]() extends IsElementOf[A, A :: As]

  /** Evidence that the type is in the list that follows the first element `B`,
    * as proven by `ev`.
    */
  final case class Tail[A, B, As <: TypeList](ev: IsElementOf[A, As])
      extends `IsElementOf`[A, B :: As]

  /** Derives [[Head]] evidence for a type that is the first element of the
    * list.
    */
  implicit def IsElementOfHead[A, As <: TypeList]: IsElementOf[A, A :: As] =
    Head[A, As]()

  /** Derives [[Tail]] evidence for a type that is in the rest of the list, from
    * the evidence for that rest.
    */
  implicit def IsElementOfTail[A, B, As <: TypeList](implicit
      ev: `IsElementOf`[A, As]
  ): `IsElementOf`[A, B :: As] =
    Tail[A, B, As](ev)
}
