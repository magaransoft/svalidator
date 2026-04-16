package com.magaran.svalidator.binding

import com.magaran.svalidator.validation.binding.BindingAndValidationResultWithoutData
import com.magaran.svalidator.validation.binding.Failure
import com.magaran.svalidator.validation.binding.Success
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.validation.ValidationMetadata
import com.magaran.svalidator.FieldKey

/** Represents the result of a binding process, either a [[BindingPass]] or a [[BindingFailure]]
  *
  * @tparam A Type of the object that was attempted to bind
  */
sealed trait BindingResult[+A]:

  /** Source of values used for the binding process */
  def source: Source

  /** Binding context passed down to the binding process */
  def context: BindingContext

  /** Converts this binding result to the equivalent BindingAndValidationResult, without any data */
  def asBindingAndValidationResult: BindingAndValidationResultWithoutData[A] =
    this match
      case p: BindingPass[A] => p.asSuccess
      case f: BindingFailure => f.asFailure

  /** Localizes all message keys of failures in this binding result, if it is a failure */
  def localize(using Localizer): BindingResult[A]

  /** Returns a Some with the result of applying the partial function to this instance if it is defined for this
    * instance, otherwise returns None
    *
    * @param pf The partial function to apply
    * @tparam B The return type of the partial function
    */
  def collect[B](pf: PartialFunction[BindingResult[A], B]): Option[B] = pf.lift(this)

/** Represents binding results that were bound successfully.  It is safely sealed for pattern matching.
  *
  * @param value Value that was bound
  * @tparam A Type of the bound value
  */
final case class BindingPass[+A](value: A)(using val source: Source, val context: BindingContext)
    extends BindingResult[A]:

  /** Converts this binding result into the equivalent binding and validation successful result */
  def asSuccess: Success[A] = Success(value)(this)

  override def localize(using Localizer): BindingPass[A] = this

/** Represents binding results that failed binding.  It is safely sealed for pattern matching.
  *
  * @param fieldErrors Errors that occurred during binding
  * @param cause       The exception that was thrown and caused the binding to fail, if any
  */
final case class BindingFailure(fieldErrors: List[FieldError], cause: Option[Throwable])(
  using val source: Source,
  val context: BindingContext
) extends BindingResult[Nothing]:

  /** Converts this binding failure into the equivalent Failure */
  def asFailure: Failure[Nothing] =
    Failure(fieldErrors.map(_.asValidationFailure))(this)(using ValidationMetadata.empty)

  override def localize(using Localizer): BindingFailure =
    BindingFailure(fieldErrors.map(_.localize), cause)

/** Provides convenience methods for creating a BindingFailure */
object BindingFailure:

  /** Creates a <code>BindingFailure</code> from the given parameters
    *
    * @param fieldKey The FieldKey used to extract values
    * @param message The message key to use for the failure
    * @param cause Throwable cause that caused the failure
    * @param source The [[Source]] from which values were extracted
    * @param context The [[BindingContext]] passed down from the call site of the binding
    */
  def apply(fieldKey: FieldKey, message: String, cause: Option[Throwable])(
    using Source,
    BindingContext
  ): BindingFailure =
    BindingFailure(List(FieldError(fieldKey, MessageParts(message), alreadyLocalized = false)), cause)

  /** Creates a <code>BindingFailure</code> from the given parameters
    *
    * @param fieldKey The FieldKey used to extract values
    * @param messageParts  The message parts to use for the failure
    * @param cause    Throwable cause that caused the failure
    * @param source The [[Source]] from which values were extracted
    * @param context The [[BindingContext]] passed down from the call site of the binding
    */
  def apply(fieldKey: FieldKey, messageParts: MessageParts, cause: Option[Throwable])(
    using Source,
    BindingContext
  ): BindingFailure =
    BindingFailure(List(FieldError(fieldKey, messageParts, alreadyLocalized = false)), cause)
