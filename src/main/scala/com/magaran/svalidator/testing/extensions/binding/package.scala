package com.magaran.svalidator.testing.extensions

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.testing.extensions
import com.magaran.svalidator.testing.extensions.common.ShouldMessageKeyContinuation
import com.magaran.svalidator.validation.binding.BindingAndValidationResultWithoutData
import com.magaran.svalidator.validation.binding.Failure
import com.magaran.svalidator.validation.binding.Success
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

package object binding:

  /** Throws an exception if at least one failure occurred in this binding and validation summary. */
  def shouldHaveValidationErrorFor[A](
    summary: BindingAndValidationResultWithoutData[A],
    fieldName: String
  ): ShouldMessageKeyContinuation =
    val targetKey = NestedFieldKey(fieldName)
    shouldHaveValidationErrorFor(summary, targetKey)

  /** Throws an exception if no errors occurred for the given <code>FieldKey</code> in this summary */
  def shouldHaveValidationErrorFor[A](
    summary: BindingAndValidationResultWithoutData[A],
    targetKey: FieldKey
  ): ShouldMessageKeyContinuation =
    val errors = summary match
      case Failure(validationFailures) =>
        validationFailures filter:
          _.fieldKey == targetKey
      case Success(_) => Nil
    if errors.isEmpty then
      throw ValidationTestingException(
        s"\nExpected instance to have errors for key $targetKey, but it didn't have any."
      )
    ShouldMessageKeyContinuation(targetKey, errors)

  /** Throws an exception if an error occurred for the given field name in this summary */
  def shouldNotHaveValidationErrorFor[A](summary: BindingAndValidationResultWithoutData[A], fieldName: String): Unit =
    val targetKey = NestedFieldKey(fieldName)
    shouldNotHaveValidationErrorFor(summary, targetKey)

  /** Throws an exception if an error occurred for the given <code>FieldKey</code> in this summary */
  def shouldNotHaveValidationErrorFor[A](summary: BindingAndValidationResultWithoutData[A], targetKey: FieldKey): Unit =
    summary match
      case Failure(validationFailures) =>
        val errors = validationFailures filter { _.fieldKey == targetKey }
        if errors.nonEmpty then
          throw ValidationTestingException(
            s"\nExpected instance to not have errors for field $targetKey, but it had the following errors:\n%s"
              .format(errors.map(_.message).mkString("\n"))
          )
      case Success(_) =>
