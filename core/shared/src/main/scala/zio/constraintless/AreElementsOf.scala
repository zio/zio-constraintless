package zio.constraintless

import TypeList.{::, End}

/** Evidence that every type in the list `T1` is an element of the list `T2`.
  * The compiler derives it from [[IsElementOf]] evidence for each type of `T1`,
  * and the code does not compile when a type of `T1` is missing from `T2`. The
  * empty list is an element-wise subset of every list.
  *
  * {{{
  * import zio.constraintless.TypeList._
  *
  * type Allowed = Int :: Boolean :: String :: End
  *
  * val evidence = AreElementsOf[Boolean :: Int :: End, Allowed]
  * // TypeCollection(Tail(Head()),TypeCollection(Head(),NilCollection()))
  * }}}
  *
  * @see
  *   [[https://zio.dev/zio-constraintless/ ZIO Constraintless]]
  */
sealed trait AreElementsOf[T1 <: TypeList, T2 <: TypeList]

/** The cases of [[AreElementsOf]] and the implicit methods that derive them.
  */
object AreElementsOf {

  /** Returns the evidence that every type of `T1` is an element of `T2`, which
    * the compiler derives. It does not compile when a type of `T1` is missing
    * from `T2`.
    */
  def apply[T1 <: TypeList, T2 <: TypeList](implicit
      ev: AreElementsOf[T1, T2]
  ): AreElementsOf[T1, T2] = ev

  /** Evidence for a non-empty list: its first type `T1` is an element of `T2`
    * (`ev1`), and so are all the types of the rest `T11` (`ev2`).
    */
  final case class TypeCollection[T1, T11 <: TypeList, T2 <: TypeList](
      ev1: T1 `IsElementOf` T2,
      ev2: T11 `AreElementsOf` T2
  ) extends AreElementsOf[T1 :: T11, T2]

  /** Evidence for the empty list, which has no type to check.
    */
  final case class NilCollection[T2 <: TypeList]()
      extends AreElementsOf[End, T2]

  /** Derives [[TypeCollection]] evidence for a non-empty list from the evidence
    * for its first type and for its remaining types.
    */
  implicit def typesElementsOfTypes[Head, Tail <: TypeList, Types <: TypeList](
      implicit
      ev1: Head `IsElementOf` Types,
      ev2: Tail `AreElementsOf` Types
  ): AreElementsOf[Head :: Tail, Types] =
    TypeCollection(ev1, ev2)

  /** Derives [[NilCollection]] evidence for the empty list.
    */
  implicit def endElementOfTypes[Types <: TypeList]: AreElementsOf[End, Types] =
    NilCollection()

}
