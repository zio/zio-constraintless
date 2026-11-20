package zio.constraintless

import `IsElementOf`._

/** Holds a `TypeClass` instance for every type in the list `As`. It lets code
  * pick the instance for a type `B` that is only known, through [[IsElementOf]]
  * evidence, to be in `As`.
  *
  * The compiler derives `Instances[TypeClass, A :: As]` when it finds a
  * `TypeClass[A]` in implicit scope and the `Instances` of the rest of the
  * list. The code does not compile if one of the types has no instance.
  *
  * {{{
  * import zio.constraintless.TypeList._
  *
  * trait Show[A] { def show(a: A): String }
  *
  * implicit val showInt: Show[Int] = new Show[Int] {
  *   def show(a: Int): String = "Int(" + a + ")"
  * }
  * implicit val showBoolean: Show[Boolean] = new Show[Boolean] {
  *   def show(a: Boolean): String = "Boolean(" + a + ")"
  * }
  *
  * type Allowed = Int :: Boolean :: End
  *
  * val instances = implicitly[Instances[Show, Allowed]]
  * val shown: String = instances.withInstance[Boolean, String](_.show(true))
  * // "Boolean(true)"
  * }}}
  *
  * @see
  *   [[https://zio.dev/zio-constraintless/ ZIO Constraintless]]
  */
trait Instances[TypeClass[_], As <: TypeList] {

  /** Calls `use` with the `TypeClass` instance for `B` and returns its result.
    * The instance is the one held for the position of `B` that `ev` proves.
    */
  def withInstance[B, D](use: TypeClass[B] => D)(implicit
      ev: B `IsElementOf` As
  ): D
}

/** The implicit methods that derive [[Instances]].
  */
object Instances {
  import TypeList._

  /** Derives the instances for the list `A :: As` from the `C[A]` instance in
    * implicit scope and the instances for `As`.
    */
  implicit def instancesCons[C[_], A, As <: TypeList](implicit
      c: C[A],
      ev: Instances[C, As]
  ): Instances[C, A :: As] = new Instances[C, A :: As] {
    override def withInstance[B, D](
        use: C[B] => D
    )(implicit ev2: `IsElementOf`[B, A :: As]): D =
      ev2 match {
        case Head() =>
          use(
            c.asInstanceOf[C[B]]
          ) // We have compile-time evidence that B is in fact A
        case Tail(x) => ev.withInstance(use)(x)
      }

  }

  // The definition is slightly different from what is mentioned in the paper where it traverses hlist
  /** Derives the instances for the empty list, which holds none. Its
    * `withInstance` throws a `RuntimeException` with the message `unreachable`,
    * because no [[IsElementOf]] evidence exists for the empty list.
    */
  implicit def instancesEnd[C[_]]: Instances[C, End] = new Instances[C, End] {
    override def withInstance[B, D](use: C[B] => D)(implicit
        ev: `IsElementOf`[B, End]
    ): D =
      sys.error("unreachable")
  }
}
