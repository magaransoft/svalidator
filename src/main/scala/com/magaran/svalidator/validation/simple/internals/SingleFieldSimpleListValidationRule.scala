package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.validation.*
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey
import com.magaran.typedmap.TypedEntry

/** Applies the contents of a [[SimpleValidationRuleStructureContainer]] to a given instance of a type
  *
  * @param lazyPropertyExtractor Function that extracts the property to be validated from the instance
  * @param ruleExpression Expression to be evaluated for validation
  * @param fieldKey Field key to use for error messages
  * @param errorMessageKey If invalid, the key to use for the error's MessageParts
  * @param errorMessageFormatValues If invalid, the format values to use for the error's MessageParts
  * @param conditionedValidation Condition to determine if the validation will be applied or not
  * @param markIndexesOfFieldNameErrors If true, the indexes of the field name will be marked in the error message
  * @param metadataFunctions Functions to generate metadata to be applied to the validation failure
  * @tparam A Type of instance being validated
  * @tparam B Type of the extracted property being validated
  */
protected[internals] final class SingleFieldSimpleListValidationRule[A, B](
  lazyPropertyExtractor: => Seq[B],
  fieldKey: FieldKey,
  ruleExpression: B => Boolean,
  errorMessageKey: String,
  errorMessageFormatValues: Option[(B, A) => Vector[Any]],
  conditionedValidation: (B, A) => Boolean,
  markIndexesOfFieldNameErrors: Boolean,
  metadataFunctions: Vector[B => TypedEntry[?]]
) extends ValidationRule[A]:

  override def apply(instance: A): Vector[ValidationFailure] =
    lazyPropertyExtractor.toVector.zipWithIndex.collect:
      case (propertyValue, index) if conditionedValidation(propertyValue, instance) && !ruleExpression(propertyValue) =>
        val keyToUse = fieldKey match
          case nested: NestedFieldKey if markIndexesOfFieldNameErrors => nested.indexed(index)
          case key                                                    => key

        val formatValues =
          errorMessageFormatValues.map(_.apply(propertyValue, instance)).getOrElse(Vector(propertyValue))
        val messageParts =
          MessageParts(messageKey = errorMessageKey, messageFormatValues = formatValues)
        val metadataEntries = metadataFunctions.map(_.apply(propertyValue))
        ValidationFailure(keyToUse, messageParts, ValidationMetadata(metadataEntries*))
