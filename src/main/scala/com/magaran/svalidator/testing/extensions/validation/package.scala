package com.magaran.svalidator.testing.extensions

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.testing.extensions.common.ShouldMessageKeyContinuation
import com.magaran.svalidator.validation.Invalid
import com.magaran.svalidator.validation.ResultWithoutData
import com.magaran.svalidator.validation.Valid
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

package object validation:

  /** Throws an exception if no errors occurred for the given field name in this summary */
  protected[validation] def shouldHaveValidationErrorFor(
    summary: ResultWithoutData,
    fieldName: String
  ): ShouldMessageKeyContinuation =

    val targetKey = NestedFieldKey(fieldName)
    shouldHaveValidationErrorFor(summary, targetKey)

  /** Throws an exception if no errors occurred for the given FieldKey in this summary */
  protected[validation] def shouldHaveValidationErrorFor(
    summary: ResultWithoutData,
    targetKey: FieldKey
  ): ShouldMessageKeyContinuation =
    val errors = summary match
      case Invalid(validationFailures) =>
        validationFailures filter:
          _.fieldKey == targetKey
      case Valid => Nil
    if errors.isEmpty then
      val extraMessage = summary match
        case Invalid(validationFailures) =>
          s"\nInstead, errors were found for the following keys: ${validationFailures.map(_.fieldKey).mkString(",")}"
        case Valid =>
          "\nNo errors were found at all in the validation summary."
      throw ValidationTestingException(s"\nExpected instance to have errors for key $targetKey. $extraMessage")
    ShouldMessageKeyContinuation(targetKey, errors)

  /** Throws an exception if an error occurred for the given field name in this summary */
  protected[validation] def shouldNotHaveValidationErrorFor(summary: ResultWithoutData, fieldName: String): Unit =
    val targetKey = NestedFieldKey(fieldName)
    shouldNotHaveValidationErrorFor(summary, targetKey)

  /** Throws an exception if an error occurred for the given FieldKey in this summary */
  protected[validation] def shouldNotHaveValidationErrorFor(summary: ResultWithoutData, targetKey: FieldKey): Unit =
    summary match
      case Invalid(validationFailures) =>
        val errors = validationFailures filter:
          _.fieldKey == targetKey
        if errors.nonEmpty then
          throw ValidationTestingException(
            s"\nExpected instance to not have errors for field $targetKey, but it had the following errors:\n%s"
              .format(errors.map(_.message).mkString("\n"))
          )
      case Valid =>
