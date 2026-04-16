package com.magaran.svalidator.testing.extensions.common

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.validation.CanFormat
import com.magaran.svalidator.validation.ValidationFailure
import com.magaran.svalidator.FieldKey

class ShouldMessageArgumentsContinuation(fieldName: FieldKey, messageKey: String, errorsOfKey: List[ValidationFailure]):

  /** Throws an exception if for the errors of the given field, with the given message key, format arguments do not match */
  infix def withFormatValues(formatValues: Product): Unit =
    val valuesList = formatValues.productIterator.toList
    if !errorsOfKey.exists(_.messageParts.messageFormatValues == valuesList) then
      throw ValidationTestingException(
        s"\nExpected instance to have errors for field $fieldName with message key $messageKey and format values " +
          s"$valuesList, but instead " +
          s"the following format values were found: ${errorsOfKey.map(_.messageParts.messageFormatValues).mkString(",")}"
      )

  /** Throws an exception if for the errors of the given field, with the given message key, format arguments do not match */
  infix def withFormatValues(formatValue: CanFormat): Unit =
    if !errorsOfKey.exists(_.messageParts.messageFormatValues == List(formatValue)) then
      throw ValidationTestingException(
        s"\nExpected instance to have errors for field $fieldName with message key $messageKey and format values " +
          s"List($formatValue), but instead " +
          s"the following format values were found: ${errorsOfKey.map(_.messageParts.messageFormatValues).mkString(",")}"
      )
