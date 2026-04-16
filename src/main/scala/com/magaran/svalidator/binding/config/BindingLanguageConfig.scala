package com.magaran.svalidator.binding.config

import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

/** Provides configuration for error messages to use in binders when binding fails */
trait BindingLanguageConfig:

  /** Returns the default message key to use when validation fails for a field and
    * [[com.magaran.svalidator.validation.simple.internals.SimpleListValidationRuleContinuationBuilder.withMessage withMessage]] was
    * not called in its validation chain.
    *
    * The property value is provided in case the message key needs to be generated dynamically based
    * on its value, but it is also passed as a message format value in the [[com.magaran.svalidator.validation.MessageParts]]
    * object that is returned in the resulting [[com.magaran.svalidator.validation.ValidationFailure ValidationFailure]].
    *
    * @param fieldKey     The key of the field that failed validation
    * @param propertyValue The value of the field that failed validation, mapped if it was part of a mapping chain
    */
  def defaultKeyForWithMessage(fieldKey: FieldKey, propertyValue: Any): String

  /** Returns message parts to use when binding fails for a type that can not be bound as a root object (usually primitives
    * or sequences)
    *
    * @param targetType The full name of the type that was attempted to be bound as a root field
    */
  def invalidRootBindingOfType(targetType: String): MessageParts

  /** Returns message parts to use when binding fails because no value was provided for a required input
    *
    * @param fieldKey The key of the field being bound
    */
  def noValueProvidedMessage(fieldKey: FieldKey): MessageParts

  /** Returns message parts to use when binding fails for a String field that is empty or whitespace
    *
    * @param fieldKey The key of the field being bound
    */
  def invalidNonEmptyStringMessage(fieldKey: FieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a boolean field
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a boolean
    */
  def invalidBooleanMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for an integer field
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as an integer
    */
  def invalidIntegerMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for an integer field
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as an integer
    */
  def invalidLongMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a float field
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a float
    */
  def invalidFloatMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a double field
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a double
    */
  def invalidDoubleMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a [[scala.BigDecimal BigDecimal]] field
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a [[scala.BigDecimal BigDecimal]]
    */
  def invalidDecimalMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a [[java.time.LocalDate LocalDate]] field, according to the
    * date format provided in the
    * [[com.magaran.svalidator.binding.config.BindingConfig BindingConfig]] passed to the
    * binder.
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a [[java.time.LocalDate LocalDate]]
    */
  def invalidDateMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a [[java.time.LocalTime LocalTime]] field, according to the
    * date format provided in the
    * [[com.magaran.svalidator.binding.config.BindingConfig BindingConfig]] passed to the
    * binder.
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a [[java.time.LocalTime LocalTime]]
    */
  def invalidTimeMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a [[java.util.UUID UUID]] field
    *
    * @param fieldKey   The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a [[java.util.UUID UUID]]
    */
  def invalidUUIDMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when binding fails for a [[scala.Enumeration]] or for an object that follows the case-object enumeration pattern
    * described in [[https://github.com/NovaMage/SValidator/wiki/Type-Based-Enumerations Type Based Enumerations]].
    *
    * Essentially, assuming the enumeration is properly constructed, binding will fail if no enumeration value exists
    * that matches a given int id, if no value is found for the field, or if a value is found but is not a valid int.
    *
    * @param fieldKey  The key of the field being bound
    * @param fieldValue The string that was attempted to be bound as a value of a enumeration
    */
  def invalidEnumerationMessage(targetType: String)(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when a sequence binding fails because the passed json string wasn't valid
    *
    * @param fieldKey  The passed in FieldKey
    * @param fieldValue The passed in json string of the target array
    */
  def invalidSequenceMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts

  /** Returns message parts to use when json binding fails because the passed json string couldn't be parsed
    *
    * @param fieldKey  The passed in FieldKey
    * @param fieldValue The passed in json string for parsing
    */
  def invalidJsonMessage(fieldKey: FieldKey, fieldValue: String): MessageParts

object BindingLanguageConfig:

  given languageConfigFromConfig(using config: BindingConfig): BindingLanguageConfig = config.languageConfig

  final val defaultConfig: BindingLanguageConfig = new BindingLanguageConfig:

    override def defaultKeyForWithMessage(fieldKey: FieldKey, value: Any): String = "invalid.value"

    override def invalidRootBindingOfType(targetType: String): MessageParts =
      MessageParts("invalid.root.binding", Vector(targetType))

    override def noValueProvidedMessage(fieldKey: FieldKey): MessageParts =
      MessageParts("required.field", Vector(fieldKey))

    def invalidNonEmptyStringMessage(fieldKey: FieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.nonempty.string", Vector(fieldValue))

    override def invalidBooleanMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.boolean", Vector(fieldValue))

    override def invalidIntegerMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.integer", Vector(fieldValue))

    override def invalidLongMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.long", Vector(fieldValue))

    override def invalidFloatMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.float", Vector(fieldValue))

    override def invalidDoubleMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.double", Vector(fieldValue))

    override def invalidDecimalMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.decimal", Vector(fieldValue))

    override def invalidEnumerationMessage(
      targetType: String
    )(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.enumeration", Vector(fieldValue, targetType))

    override def invalidSequenceMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.sequence", Vector(fieldValue))

    override def invalidDateMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.date", Vector(fieldValue))

    override def invalidTimeMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.time", Vector(fieldValue))

    override def invalidUUIDMessage(fieldKey: NestedFieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.uuid", Vector(fieldValue))

    override def invalidJsonMessage(fieldKey: FieldKey, fieldValue: String): MessageParts =
      MessageParts("invalid.json", Vector(fieldValue))
