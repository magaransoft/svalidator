package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingFailure
import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.binding.BindingResult
import com.magaran.svalidator.binding.BoundIgnoredAsEmpty
import com.magaran.svalidator.binding.CanBeBoundIgnoredAsEmpty
import com.magaran.svalidator.binding.Source
import com.magaran.svalidator.FieldKey

/** Binder that binds a [[Source]] into a case class `A` by recursively binding each field using its corresponding
  * [[TypedBinder]], then assembling the result via the case class constructor.
  *
  * @tparam A the target case class type
  * @param creatorFunction the case class factory (typically `fromProduct` from the companion)
  * @param fieldNames the ordered list of field names in the case class
  * @param fieldBinders the ordered list of binders for each field
  */
final class ProductBinder[A](
  creatorFunction: Product => A,
  fieldNames: List[String],
  fieldBinders: List[TypedBinder[?]]
) extends TypedBinder[A]:

  private final val fieldNamesWithBinders = fieldNames.zip(fieldBinders)

  def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[A] =
    val (errorList, argsList) = fieldNamesWithBinders
      .map {
        case (fieldName, fieldBinder) =>
          fieldBinder.bind(source.downField(fieldName), fieldKey.downField(fieldName))
      }
      .partitionMap:
        case BindingPass(value) => Right(value)
        case f: BindingFailure  => Left(f)
    errorList match
      case Nil => BindingPass(creatorFunction(toTuple(argsList, EmptyTuple)))(using source)
      case nonEmptyList =>
        val fieldErrors = nonEmptyList.flatMap(_.fieldErrors)
        val causes      = nonEmptyList.flatMap(_.cause)
        val cause =
          if causes.forall(_.isInstanceOf[NoSuchElementException]) && argsList.forall(isEmptyOrIgnoredForBinding)
          then Some(new NoSuchElementException())
          else None

        BindingFailure(fieldErrors, cause)(using source)
  end bind

  private def isEmptyOrIgnoredForBinding(value: Any): Boolean = value match
    case x: IterableOnce[?]          => x.iterator.isEmpty
    case _: BoundIgnoredAsEmpty      => true
    case x: CanBeBoundIgnoredAsEmpty => x.isEmpty
    case _                           => false

  private def toTuple(xs: List[?], acc: Tuple): Tuple =
    xs match
      case Nil    => acc
      case h :: t => h *: toTuple(t, acc)

end ProductBinder
