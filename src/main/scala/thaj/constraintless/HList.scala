package thaj.constraintless

sealed trait HList

final case class HCons[H, T <: HList](head: H, tail: T) extends HList {
  def ::[T](v: T) = HCons(v, this)
}

case class HNil() extends HList {
  def ::[T](v: T) = HCons(v, this)
}

// aliases for building HList types and for pattern matching
object HList {
  type ::[H, T <: HList] = HCons[H, T]
  val :: = HCons
  // def ::[H, T <: HList] = HCons[H, T]
}
