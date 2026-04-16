package com.magaran.svalidator.evidences

import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.validation.*
import com.magaran.svalidator.validation.binding.*

/** Provides evidence that a given type of [[com.magaran.svalidator.validation.ValidationResult]] can be
  * converted to a given type of
  * [[com.magaran.svalidator.validation.binding.BindingAndValidationResult]]
  */
trait CanConvert[A, B <: ValidationResult, C <: BindingAndValidationResult[A]]:

  /** Converts a [[com.magaran.svalidator.validation.ValidationResult ValidationResult]] into a [[com.magaran.svalidator.validation.binding.BindingAndValidationResult]] using the provided values
    *
    * @param value The value that was validated
    * @param bindingResult The result of the binding process
    * @param validationResult The result of the validation process
    * @param localizer The localizer to use for localizing messages
    * @return The converted <code>BindingAndValidationResult</code> depending on the type of <code>ValidationResult</code>
    */
  def convert(value: A, bindingResult: BindingPass[?], validationResult: B, localizer: Localizer): C

/** Provides evidence implementations of [[com.magaran.svalidator.validation.ValidationResult]] that can be
  * converted into [[com.magaran.svalidator.validation.binding.BindingAndValidationResult]]
  */
object CanConvert:

  /** Evidence that converts a [[com.magaran.svalidator.validation.ResultWithoutData]] into a
    * [[com.magaran.svalidator.validation.binding.BindingAndValidationResultWithoutData]]
    */
  given [A]: CanConvert[A, ResultWithoutData, BindingAndValidationResultWithoutData[A]] =
    (value: A, bindingResult: BindingPass[?], validationResult: ResultWithoutData, localizer: Localizer) =>
      validationResult match
        case Valid => Success(value)(bindingResult)
        case f: Invalid =>
          Failure(f.validationFailures.map(_.localize(using localizer)))(bindingResult)(using f.validationMetadata)

  /** Evidence that converts a [[com.magaran.svalidator.validation.ResultWithSuccessData]] into a
    * [[com.magaran.svalidator.validation.binding.BindingAndValidationResultWithSuccessData]]
    */
  given [I, D]: CanConvert[I, ResultWithSuccessData[D], BindingAndValidationResultWithSuccessData[I, D]] =
    (value: I, bindingResult: BindingPass[?], validationResult: ResultWithSuccessData[D], localizer: Localizer) =>
      validationResult match
        case Valid(data) => SuccessWithData(value, data)(bindingResult)
        case f: Invalid =>
          Failure(f.validationFailures.map(_.localize(using localizer)))(bindingResult)(using f.validationMetadata)
