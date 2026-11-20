package zio.constraintless

/**
 * A type-level list of types. It has no values: it exists so that the compiler
 * can track which types a data type may contain. A list is built from
 * [[TypeList.::]] and ends with [[TypeList.End]], for example
 * `Int :: Boolean :: End`.
 *
 * {{{
 * import zio.constraintless.TypeList._
 *
 * type Allowed = Int :: Boolean :: String :: End
 * }}}
 *
 * @see [[https://zio.dev/zio-constraintless/ ZIO Constraintless]]
 */
sealed trait TypeList

/**
 * The constructors of [[TypeList]].
 */
object TypeList {

  /**
   * A non-empty list whose first type is `H` and whose remaining types are the
   * list `T`. As a type with two parameters it can be written infix and it
   * associates to the right: `A :: B :: End`.
   */
  sealed trait ::[H, T <: TypeList] extends TypeList

  /**
   * The empty list, which ends every [[TypeList]].
   */
  sealed trait End extends TypeList
}
