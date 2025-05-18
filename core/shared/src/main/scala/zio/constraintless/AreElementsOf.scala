package zio.constraintless

import TypeList.{::, End}

sealed trait AreElementsOf[T1 <: TypeList, T2 <: TypeList]

object AreElementsOf {
  def apply[T1 <: TypeList, T2 <: TypeList](implicit
      ev: AreElementsOf[T1, T2]
  ): AreElementsOf[T1, T2] = ev

  final case class TypeCollection[T1, T11 <: TypeList, T2 <: TypeList](
      ev1: T1 IsElementOf T2,
      ev2: T11 AreElementsOf T2
  ) extends AreElementsOf[T1 :: T11, T2]

  final case class NilCollection[T2 <: TypeList]()
      extends AreElementsOf[End, T2]

  implicit def typesElementsOfTypes[Head, Tail <: TypeList, Types <: TypeList](
      implicit
      ev1: Head IsElementOf Types,
      ev2: Tail AreElementsOf Types
  ): AreElementsOf[Head :: Tail, Types] =
    TypeCollection(ev1, ev2)

  implicit def endElementOfTypes[Types <: TypeList]: AreElementsOf[End, Types] =
    NilCollection()

}
