package com.magaran.svalidator.binding.binders

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

import scala.quoted.Expr
import scala.quoted.Quotes
import scala.quoted.Type
import scala.reflect.ClassTag

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.special.*
import com.magaran.svalidator.binding.binders.typed.*
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/**  Takes values from [[Source]] identified under a given [[FieldKey]] and converts them into an instance of the
  * target type <code>A</code>
  *
  * @tparam A Type of the resulting bound value
  */
trait TypedBinder[A]:

  /** Attempts to bind an instance of <code>A</code> using the given parameters
    *
    * @param source The [[Source]] used to extract values for binding
    * @param fieldKey The [[FieldKey]] used to target values from the source
    * @param config The [[com.magaran.svalidator.binding.config.BindingConfig]] to use in binders
    * @param context The [[BindingContext]] information provided by the call site of the binding
    * @return [[com.magaran.svalidator.binding.BindingPass BindingPass]] with the bound value if successful, [[com.magaran.svalidator.binding.BindingFailure BindingFailure]] with errors and throwable cause otherwise
    */
  def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[A]

  /** Creates a new binder that will map the bound value of the original binder to a new value using the given function.
    * In case of failure, the new binder will return the same failure as the original binder.
    * @param f Function to apply to the bound value of the original binder in case of successful binding
    * @tparam B Type of the resulting bound value
    * @return A new binder that will bind values of type B
    */
  def compose[B](f: A => B): TypedBinder[B] = new TypedBinder[B]:
    override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[B] =
      given Source = source
      TypedBinder.this.bind(source, fieldKey) match
        case BindingPass(value)      => BindingPass(f(value))
        case failure: BindingFailure => failure

object TypedBinder:

  // Typed binders for primitive types
  given bigDecimalBinder: TypedBinder[BigDecimal] = BigDecimalBinder()
  given booleanBinder: TypedBinder[Boolean]       = BooleanBinder()
  given doubleBinder: TypedBinder[Double]         = DoubleBinder()
  given floatBinder: TypedBinder[Float]           = FloatBinder()
  given intBinder: TypedBinder[Int]               = IntBinder()
  given localDateBinder: TypedBinder[LocalDate]   = LocalDateBinder()
  given localTimeBinder: TypedBinder[LocalTime]   = LocalTimeBinder()
  given longBinder: TypedBinder[Long]             = LongBinder()
  given stringBinder: TypedBinder[String]         = StringBinder()
  given uuidBinder: TypedBinder[UUID]             = UUIDBinder()

  // Special binders related to functors
  given optionBinder[A: TypedBinder]: TypedBinder[Option[A]]                                       = OptionBinder()
  given listBinder[A: TypedBinder](using TypeShow[List[A]]): TypedBinder[List[A]]                  = ListBinder()
  given arrayBinder[A: { TypedBinder, ClassTag }](using TypeShow[Array[A]]): TypedBinder[Array[A]] = ArrayBinder()
  given setBinder[A: TypedBinder](using TypeShow[Set[A]]): TypedBinder[Set[A]]                     = SetBinder()
  given vectorBinder[A: TypedBinder](using TypeShow[Vector[A]]): TypedBinder[Vector[A]]            = VectorBinder()

  // Optimization of special binders for primitive types
  given optionBigDecimalBinder: TypedBinder[Option[BigDecimal]] = summon[TypedBinder[Option[BigDecimal]]]
  given optionBooleanBinder: TypedBinder[Option[Boolean]]       = summon[TypedBinder[Option[Boolean]]]
  given optionDoubleBinder: TypedBinder[Option[Double]]         = summon[TypedBinder[Option[Double]]]
  given optionFloatBinder: TypedBinder[Option[Float]]           = summon[TypedBinder[Option[Float]]]
  given optionIntBinder: TypedBinder[Option[Int]]               = summon[TypedBinder[Option[Int]]]
  given optionLocalDateBinder: TypedBinder[Option[LocalDate]]   = summon[TypedBinder[Option[LocalDate]]]
  given optionLocalTimeBinder: TypedBinder[Option[LocalTime]]   = summon[TypedBinder[Option[LocalTime]]]
  given optionLongBinder: TypedBinder[Option[Long]]             = summon[TypedBinder[Option[Long]]]
  given optionStringBinder: TypedBinder[Option[String]]         = summon[TypedBinder[Option[String]]]
  given optionUUIDBinder: TypedBinder[Option[UUID]]             = summon[TypedBinder[Option[UUID]]]

  given listBigDecimalBinder: TypedBinder[List[BigDecimal]] = summon[TypedBinder[List[BigDecimal]]]
  given listBooleanBinder: TypedBinder[List[Boolean]]       = summon[TypedBinder[List[Boolean]]]
  given listDoubleBinder: TypedBinder[List[Double]]         = summon[TypedBinder[List[Double]]]
  given listFloatBinder: TypedBinder[List[Float]]           = summon[TypedBinder[List[Float]]]
  given listIntBinder: TypedBinder[List[Int]]               = summon[TypedBinder[List[Int]]]
  given listLocalDateBinder: TypedBinder[List[LocalDate]]   = summon[TypedBinder[List[LocalDate]]]
  given listLocalTimeBinder: TypedBinder[List[LocalTime]]   = summon[TypedBinder[List[LocalTime]]]
  given listLongBinder: TypedBinder[List[Long]]             = summon[TypedBinder[List[Long]]]
  given listStringBinder: TypedBinder[List[String]]         = summon[TypedBinder[List[String]]]
  given listUUIDBinder: TypedBinder[List[UUID]]             = summon[TypedBinder[List[UUID]]]

  given arrayBigDecimalBinder: TypedBinder[Array[BigDecimal]] = summon[TypedBinder[Array[BigDecimal]]]
  given arrayBooleanBinder: TypedBinder[Array[Boolean]]       = summon[TypedBinder[Array[Boolean]]]
  given arrayDoubleBinder: TypedBinder[Array[Double]]         = summon[TypedBinder[Array[Double]]]
  given arrayFloatBinder: TypedBinder[Array[Float]]           = summon[TypedBinder[Array[Float]]]
  given arrayIntBinder: TypedBinder[Array[Int]]               = summon[TypedBinder[Array[Int]]]
  given arrayLocalDateBinder: TypedBinder[Array[LocalDate]]   = summon[TypedBinder[Array[LocalDate]]]
  given arrayLocalTimeBinder: TypedBinder[Array[LocalTime]]   = summon[TypedBinder[Array[LocalTime]]]
  given arrayLongBinder: TypedBinder[Array[Long]]             = summon[TypedBinder[Array[Long]]]
  given arrayStringBinder: TypedBinder[Array[String]]         = summon[TypedBinder[Array[String]]]
  given arrayUUIDBinder: TypedBinder[Array[UUID]]             = summon[TypedBinder[Array[UUID]]]

  given setBigDecimalBinder: TypedBinder[Set[BigDecimal]] = summon[TypedBinder[Set[BigDecimal]]]
  given setBooleanBinder: TypedBinder[Set[Boolean]]       = summon[TypedBinder[Set[Boolean]]]
  given setDoubleBinder: TypedBinder[Set[Double]]         = summon[TypedBinder[Set[Double]]]
  given setFloatBinder: TypedBinder[Set[Float]]           = summon[TypedBinder[Set[Float]]]
  given setIntBinder: TypedBinder[Set[Int]]               = summon[TypedBinder[Set[Int]]]
  given setLocalDateBinder: TypedBinder[Set[LocalDate]]   = summon[TypedBinder[Set[LocalDate]]]
  given setLocalTimeBinder: TypedBinder[Set[LocalTime]]   = summon[TypedBinder[Set[LocalTime]]]
  given setLongBinder: TypedBinder[Set[Long]]             = summon[TypedBinder[Set[Long]]]
  given setStringBinder: TypedBinder[Set[String]]         = summon[TypedBinder[Set[String]]]
  given setUUIDBinder: TypedBinder[Set[UUID]]             = summon[TypedBinder[Set[UUID]]]

  given vectorBigDecimalBinder: TypedBinder[Vector[BigDecimal]] = summon[TypedBinder[Vector[BigDecimal]]]
  given vectorBooleanBinder: TypedBinder[Vector[Boolean]]       = summon[TypedBinder[Vector[Boolean]]]
  given vectorDoubleBinder: TypedBinder[Vector[Double]]         = summon[TypedBinder[Vector[Double]]]
  given vectorFloatBinder: TypedBinder[Vector[Float]]           = summon[TypedBinder[Vector[Float]]]
  given vectorIntBinder: TypedBinder[Vector[Int]]               = summon[TypedBinder[Vector[Int]]]
  given vectorLocalDateBinder: TypedBinder[Vector[LocalDate]]   = summon[TypedBinder[Vector[LocalDate]]]
  given vectorLocalTimeBinder: TypedBinder[Vector[LocalTime]]   = summon[TypedBinder[Vector[LocalTime]]]
  given vectorLongBinder: TypedBinder[Vector[Long]]             = summon[TypedBinder[Vector[Long]]]
  given vectorStringBinder: TypedBinder[Vector[String]]         = summon[TypedBinder[Vector[String]]]
  given vectorUUIDBinder: TypedBinder[Vector[UUID]]             = summon[TypedBinder[Vector[UUID]]]

  // Macros for deriving binders for case classes and enums with an id field

  // Derivation macro entry point
  inline def derived[A]: TypedBinder[A] = ${
    performDerive[A]
  }

  private def performDerive[A: Type](using Quotes): Expr[TypedBinder[A]] = {
    TypedBinderMacros.performDerive[A]
  }

end TypedBinder
