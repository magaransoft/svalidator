package com.magaran.svalidator.binding

import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.validation.ValidationFailure
import com.magaran.svalidator.validation.ValidationMetadata
import com.magaran.svalidator.FieldKey

/** Contains information of errors for fields during binding
  *
  * @param fieldKey    [[FieldKey]]  that caused the error
  * @param messageParts Error message information for the field
  */
final case class FieldError(fieldKey: FieldKey, messageParts: MessageParts, alreadyLocalized: Boolean):

  /** Returns the message key of this object's [[com.magaran.svalidator.validation.MessageParts MessageParts]]
    * formatted alongside the format values using [[scala.collection.StringOps#format StringLike.format]]'s method.
    */
  def message: String = messageParts.messageKey.format(messageParts.messageFormatValues*)

  /** Returns a field error with localized messageParts generated passing the given localizer to
    * [[com.magaran.svalidator.validation.MessageParts#localize MessageParts.localize]]'s method.
    *
    * @param localizer Localizer to apply to the [[com.magaran.svalidator.validation.MessageParts MessageParts]]
    */
  def localize(using localizer: Localizer): FieldError =
    if alreadyLocalized then this
    else FieldError(fieldKey, messageParts.localize, alreadyLocalized = true)

  /** Converts this field error to an equivalent validation failure */
  def asValidationFailure: ValidationFailure =
    ValidationFailure(fieldKey, messageParts, ValidationMetadata.empty, alreadyLocalized = alreadyLocalized)
