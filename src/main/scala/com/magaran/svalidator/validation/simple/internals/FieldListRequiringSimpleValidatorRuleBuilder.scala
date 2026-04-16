package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

/** Chain builder that requires providing a field name for the generation of error messages further on
  *
  * @tparam A Type of the instance being validated
  * @tparam B Type of the extracted property being validated
  */
protected[internals] final class FieldListRequiringSimpleValidatorRuleBuilder[A, B](
  propertyListExpression: A => Seq[B],
  markIndexesOfErrors: Boolean,
  dependsOnUpstream: Option[RuleBuilderWithMessageSet[A]]
):

  /** Applies the given [[FieldKey]] for any error messages generated during this chain builder.
    *
    * @param fieldKey Field key to use for error messages
    */
  infix def ForField(fieldKey: FieldKey): SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing] =
    SimpleListValidationRuleMappableStarterBuilder(
      fieldsInformation = Vector(
        SingleFieldInformationContainer(
          propertyListExpression = propertyListExpression,
          fieldKey = fieldKey,
          previousMappedBuilderInChain = dependsOnUpstream,
          previousMappedProviderAndConverter = None,
          fieldValidationChainId = FieldValidationChainId()
        )
      ),
      markIndexesOfFieldNameErrors = markIndexesOfErrors,
    )

  /** Applies the given string as the field name for any error messages generated during this chain builder.
    *
    * @param fieldName fieldName to use in the [[FieldKey]] associated with the error messages
    */
  infix def ForField(fieldName: String): SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing] = ForField(
    NestedFieldKey(fieldName)
  )
