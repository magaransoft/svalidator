package com.magaran.svalidator.testing.extensions.common

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.validation.ValidationFailure
import com.magaran.svalidator.FieldKey
import org.jetbrains.annotations.PropertyKey

class ShouldMessageKeyContinuation(fieldKey: FieldKey, fieldFailures: List[ValidationFailure]):

  /** Throws an exception if for the errors of the given field, the message key assigned to it does not match the passed
    * in message key
    */
  infix def withMessageKey(
    @PropertyKey(resourceBundle = "messages") messageKey: String
  ): ShouldMessageArgumentsContinuation =
    val errorsOfKey = fieldFailures.filter { _.messageParts.messageKey == messageKey }
    if errorsOfKey.isEmpty then
      throw ValidationTestingException(
        s"\nExpected instance to have errors for field $fieldKey with message key $messageKey, but instead " +
          s"the following message keys were found: ${fieldFailures.map(_.messageParts.messageKey)}"
      )
    ShouldMessageArgumentsContinuation(fieldKey, messageKey, errorsOfKey)
