package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

/** Chain builder that requires providing a [[FieldKey]] for the generation of error messages further on
  *
  * @tparam A Type of the instance being validated
  * @tparam B Type of the extracted component property being validated
  */
protected[internals] final class ComponentListFieldRequiringSimpleValidatorRuleBuilder[A, B](
  componentListPropertyExpression: A => Seq[B],
  markIndexesOfFieldNameErrors: Boolean
):

  /** Applies the given <code>String</code> as the [[FieldKey]] for any error messages generated during this chain builder.
    *
    * @param fieldKey Field key to use for error messages
    */
  infix def ForField(fieldKey: FieldKey): ComponentListValidationRuleBuilder[A, B] =
    ComponentListValidationRuleBuilder[A, B](componentListPropertyExpression, fieldKey, markIndexesOfFieldNameErrors)

  /** Applies the given <code>String</code> as the [[FieldKey]] for any error messages generated during this chain builder.
    *
    * @param fieldName Field name to use for error messages
    */
  infix def ForField(fieldName: String): ComponentListValidationRuleBuilder[A, B] = ForField(NestedFieldKey(fieldName))
