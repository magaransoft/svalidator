package com.magaran.svalidator.validation.simple.internals

import com.magaran.typedmap.TypedEntry

/** Container for the structure of a validation rule that is build fluently
  *
  * @param validationExpression The expression that will be applied to the property to validate it
  * @param conditionalValidation An optional expression that will be applied to the property to determine if the validation should be applied
  * @param errorMessageKey The key of the error message to use if the validation fails
  * @param errorMessageFormatValues The format values to use for the error message if the validation fails
  * @param metadataFunctions Metadata to be applied to the validation failure
  * @tparam A Type of the instance being validated
  * @tparam B Type of the extracted property being validated
  */
protected[internals] final case class SimpleValidationRuleStructureContainer[A, B](
  validationExpression: B => Boolean,
  conditionalValidation: Option[(B, A) => Boolean],
  errorMessageKey: Option[String],
  errorMessageFormatValues: Option[(B, A) => Vector[Any]],
  metadataFunctions: Vector[B => TypedEntry[?]]
)
