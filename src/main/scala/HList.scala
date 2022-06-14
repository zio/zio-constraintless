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

object ex {

  import HList._
  // construct an HList similar to the Tuple3 ("str", true, 1.0)
  val x = "str" :: true :: 1.0 :: HNil()

// get the components by calling head/tail
  val s: String = x.head
  val b: Boolean = x.tail.head
  val d: Double = x.tail.tail.head
  // compile error
//val e = x.tail.tail.tail.head

// or, decompose with a pattern match

  val f: (String :: Boolean :: Double :: HNil) => String = {
    case "s" :: false :: _          => "test"
    case h :: true :: 1.0 :: HNil() => h
    // compilation error because of individual type mismatches and length mismatch
    // case 3 :: "i" :: HNil => "invalid"
    case _ => sys.error("unknown")
  }
}
