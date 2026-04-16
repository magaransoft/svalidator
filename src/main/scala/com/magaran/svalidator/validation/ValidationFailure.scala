package com.magaran.svalidator.validation

import com.magaran.svalidator.FieldKey

/** Contains information about a single error that occurred during validation
  *
  * @param fieldKey     Field key of the field that caused the error
  * @param messageParts The error message string and any format values
  * @param metadata     Additional metadata specific to the error that occurred (i.e. Error codes)
  */
final case class ValidationFailure(
  fieldKey: FieldKey,
  messageParts: MessageParts,
  metadata: ValidationMetadata,
  alreadyLocalized: Boolean = false
):

  /** Returns the message key of this object's [[com.magaran.svalidator.validation.MessageParts MessageParts]]
    * formatted alongside the format values using [[scala.collection.StringOps#format StringLike.format]]'s method.
    */
  def message: String = messageParts.message

  /** Returns a failure with localized messageParts generated passing the given localizer to
    * [[com.magaran.svalidator.validation.MessageParts#localize MessageParts.localize]]'s method.
    *
    * @param localizer Localizer to apply to the [[com.magaran.svalidator.validation.MessageParts MessageParts]]
    */
  def localize(using localizer: Localizer): ValidationFailure =
    if (alreadyLocalized || localizer.eq(Localizer.NoLocalization)) {
      this
    } else {
      ValidationFailure(fieldKey, messageParts.localize, metadata, alreadyLocalized = true)
    }

  override def toString: String =
    s"ValidationFailure($fieldKey -> $message)"

object ValidationFailure:

  /** Helper to generate a failure using field information
    *
    * @param field Field that caused the error
    * @param messageParts The error message string and any format values
    * @param metadata     Additional metadata specific to the error that occurred (i.e. Error codes)
    */
  def apply(field: FieldInfo[?, ?], messageParts: MessageParts, metadata: ValidationMetadata): ValidationFailure =
    ValidationFailure(field.fieldKey, messageParts, metadata)

  /** Helper to generate a failure using field information
    *
    * @param field Field that caused the error
    * @param messageParts The error message string and any format values
    * @return A validation failure with the given field name and message key
    */
  def apply(field: FieldInfo[?, ?], messageParts: MessageParts): ValidationFailure =
    ValidationFailure(field.fieldKey, messageParts, ValidationMetadata.empty)

  /** Helper to generate a failure using field information
    *
    * @param field  field that caused the error
    * @param message   Message key of the error
    * @param metadata  Additional metadata specific to the error that occurred (i.e. Error codes)
    * @return A validation failure with the given field name and message key
    */
  def apply(field: FieldInfo[?, ?], message: String, metadata: ValidationMetadata): ValidationFailure =
    ValidationFailure(field.fieldKey, MessageParts(message), metadata)

  /** Helper to generate a failure using field information
    *
    * @param field Field that caused the error
    * @param message   Message key of the error
    * @return A validation failure with the given field name and message key
    */
  def apply(field: FieldInfo[?, ?], message: String): ValidationFailure =
    ValidationFailure(field.fieldKey, MessageParts(message), ValidationMetadata.empty)

  /** Helper to generate a failure using a field key
    *
    * @param fieldName Name of the field that caused the error
    * @param messageParts The error message string and any format values
    * @return A validation failure with the given field name and message key
    */
  def apply(fieldName: FieldKey, messageParts: MessageParts): ValidationFailure =
    ValidationFailure(fieldName, messageParts, ValidationMetadata.empty)

  /** Helper to generate a failure using a field key
    *
    * @param fieldName Name of the field that caused the error
    * @param message   Message key of the error
    * @param metadata  Additional metadata specific to the error that occurred (i.e. Error codes)
    * @return A validation failure with the given field name and message key
    */
  def apply(fieldName: FieldKey, message: String, metadata: ValidationMetadata): ValidationFailure =
    ValidationFailure(fieldName, MessageParts(message), metadata)

  /** Helper to generate a failure using a field key
    *
    * @param fieldName Name of the field that caused the error
    * @param message   Message key of the error
    * @return A validation failure with the given field name and message key
    */
  def apply(fieldName: FieldKey, message: String): ValidationFailure =
    ValidationFailure(fieldName, MessageParts(message), ValidationMetadata.empty)
