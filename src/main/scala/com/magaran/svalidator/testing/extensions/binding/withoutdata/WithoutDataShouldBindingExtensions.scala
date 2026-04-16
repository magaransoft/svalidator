package com.magaran.svalidator.testing.extensions.binding.withoutdata

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.testing.extensions
import com.magaran.svalidator.testing.extensions.binding
import com.magaran.svalidator.testing.extensions.common.ShouldMessageKeyContinuation
import com.magaran.svalidator.validation.binding.BindingAndValidationResultWithoutData
import com.magaran.svalidator.validation.binding.Failure
import com.magaran.svalidator.validation.binding.Success
import com.magaran.svalidator.FieldKey

extension [A](summary: BindingAndValidationResultWithoutData[A])

  /** Throws an exception if at least one failure occurred in this binding and validation summary. */
  def shouldBeValid(): Unit =
    summary match
      case Failure(validationFailures) =>
        throw ValidationTestingException(
          "\nExpected instance to be valid, but it had the following errors:\n%s"
            .format(validationFailures.map(x => s"${x.fieldKey}->${x.message}").mkString("\n"))
        )
      case Success(_) =>

  /** Throws an exception if no errors occurred for the given field name in this summary */
  infix def shouldHaveValidationErrorFor(fieldName: String): ShouldMessageKeyContinuation =
    binding.shouldHaveValidationErrorFor(summary, fieldName)

  /** Throws an exception if no errors occurred for the given <code>FieldKey</code> in this summary */
  infix def shouldHaveValidationErrorFor(targetKey: FieldKey): ShouldMessageKeyContinuation =
    binding.shouldHaveValidationErrorFor(summary, targetKey)

  /** Throws an exception if an error occurred for the given field name in this summary */
  infix def shouldNotHaveValidationErrorFor(fieldName: String): Unit =
    binding.shouldNotHaveValidationErrorFor(summary, fieldName)

  /** Throws an exception if an error occurred for the given <code>FieldKey</code> in this summary */
  infix def shouldNotHaveValidationErrorFor(targetKey: FieldKey): Unit =
    binding.shouldNotHaveValidationErrorFor(summary, targetKey)
