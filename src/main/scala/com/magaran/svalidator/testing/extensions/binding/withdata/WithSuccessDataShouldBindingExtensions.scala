package com.magaran.svalidator.testing.extensions.binding.withdata

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.testing.extensions.binding
import com.magaran.svalidator.testing.extensions.common.ShouldBeValidWithDataContinuationExtensions
import com.magaran.svalidator.testing.extensions.common.ShouldMessageKeyContinuation
import com.magaran.svalidator.validation.binding.BindingAndValidationResultWithSuccessData
import com.magaran.svalidator.validation.binding.Failure
import com.magaran.svalidator.validation.binding.SuccessWithData
import com.magaran.svalidator.FieldKey

extension [A, B](summary: BindingAndValidationResultWithSuccessData[A, B])

  /** Throws an exception if at least one failure occurred in this validation summary. */
  def shouldBeValid(): ShouldBeValidWithDataContinuationExtensions[B] =
    summary match
      case Failure(validationFailures) =>
        throw ValidationTestingException(
          "\nExpected instance to be valid, but it had the following errors:\n%s"
            .format(validationFailures.map(x => s"${x.fieldKey}->${x.message}").mkString("\n"))
        )
      case SuccessWithData(_, data) => ShouldBeValidWithDataContinuationExtensions(data)

  /** Throws an exception if no errors occurred for the given field name in this summary */
  def shouldHaveValidationErrorFor(fieldName: String): ShouldMessageKeyContinuation =
    binding.shouldHaveValidationErrorFor(summary.withoutData, fieldName)

  /** Throws an exception if no errors occurred for the given <code>FieldKey</code> in this summary */
  def shouldHaveValidationErrorFor(targetKey: FieldKey): ShouldMessageKeyContinuation =
    binding.shouldHaveValidationErrorFor(summary.withoutData, targetKey)

  /** Throws an exception if an error occurred for the given field name in this summary */
  def shouldNotHaveValidationErrorFor(fieldName: String): Unit =
    binding.shouldNotHaveValidationErrorFor(summary.withoutData, fieldName)

  /** Throws an exception if an error occurred for the given <code>FieldKey</code> in this summary */
  def shouldNotHaveValidationErrorFor(targetKey: FieldKey): Unit =
    binding.shouldNotHaveValidationErrorFor(summary.withoutData, targetKey)
