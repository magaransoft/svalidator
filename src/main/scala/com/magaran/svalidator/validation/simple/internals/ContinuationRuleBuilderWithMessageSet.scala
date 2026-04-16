package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.FieldKey

protected[internals] trait ContinuationRuleBuilderWithMessageSet[A, B, +C](
  wrapped: SimpleListValidationRuleContinuationBuilder[A, B, C]
) extends RuleBuilderWithMessageSet[A]
    with UpstreamLazyValueProvider[Vector[B]] {

  /** Generates a rule that will cause a validation error lazily evaluating the boolean expression returns true
    *
    * @param lazyBooleanValue Expression to apply
    */
  infix final def must(lazyBooleanValue: => Boolean): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val composedFunction = (_: B) => lazyBooleanValue
    wrapped.internalMust(composedFunction)

  /** Generates a rule that will cause a validation error if applying this function to the property returns true
    *
    * @param ruleExpressionReceivingPropertyValue Expression to apply
    */
  infix def must(
    ruleExpressionReceivingPropertyValue: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    wrapped.internalMust(ruleExpressionReceivingPropertyValue)

  /** Generates a rule that will cause a validation error lazily evaluating the boolean expression returns true
    *
    * @param lazyBooleanValue Expression to apply
    */
  infix final def mustNot(lazyBooleanValue: => Boolean): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val composedFunction = (_: B) => lazyBooleanValue
    wrapped.internalMust(composedFunction.andThen(!_))

  /** Generates a rule that will cause a validation error if applying this function to the property returns false
    *
    * @param ruleExpressionReceivingPropertyValue Expression to apply
    */
  infix def mustNot(
    ruleExpressionReceivingPropertyValue: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    wrapped.internalMust(ruleExpressionReceivingPropertyValue.andThen(!_))

  /** Converts the extracted property of the preceding chain by applying <strong>once</strong> the function <code>f</code> only if all the preceding
    * validations are successful.  Further calls down the chain will work the type of the converted value.
    *
    * @param f Function to convert the extracted property
    * @tparam D The new type of the property chain
    */
  infix def map[D](f: B => D): SimpleListValidationRuleStarterBuilder[A, D, B] = wrapped.map(f, this)

  /** Switches into a different property on the same chained validation stream
    *
    * If any previous validations before switching field fail, any further statements will not be evaluated.
    *
    * @param expression The extractor for the new property to switch into
    * @tparam D The type of the new property
    */
  infix def andRuleFor[D](expression: A => D): FieldListRequiringSimpleValidatorRuleBuilder[A, D] =
    wrapped.andFor(expression, this)

  /** Switches the [[com.magaran.svalidator.FieldKey]] to use for validation errors from this point on.  The switch will be done at current
    * nesting level, if the current [[com.magaran.svalidator.FieldKey]] had any parents, it will be assumed the passed in field name resides
    * within the same parent.
    *
    * @param newFieldName Field name that will be used for errors from this point on
    */
  infix def switchFieldTo(newFieldName: String): SimpleListValidationRuleMappableStarterBuilder[A, B, B] =
    wrapped.switchFieldTo(newFieldName, this)

  /** Switches the [[com.magaran.svalidator.FieldKey]] used for errors to the given key for any validations that occur further down the chain.
    *
    * @param fieldKey Field key that will be used for errors from this point on
    */
  infix def switchFieldTo(fieldKey: FieldKey): SimpleListValidationRuleMappableStarterBuilder[A, B, B] =
    wrapped.switchFieldTo(fieldKey, this)

  protected[validation] override def buildRules(instance: A): RuleStreamCollection[A] =
    wrapped.uncheckedBuildRules(instance)

  protected[internals] override def fetchValue(chainId: FieldValidationChainId): Vector[B] = wrapped.fetchValue(chainId)
}
