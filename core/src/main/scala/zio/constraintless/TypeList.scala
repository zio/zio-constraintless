package zio.constraintless

sealed trait TypeList

object TypeList {
  sealed trait ::[H, T <: TypeList] extends TypeList
  sealed trait End extends TypeList
}
