package com.magaran.svalidator.validation.simple.internals

/** Builder that applies a boolean function to validate an extracted property
  *
  * @tparam A Type of the instance being validated
  * @tparam B Type of the extracted property being validated
  * @tparam C If the property has been mapped, type the property had before the map, otherwise, [[scala.Nothing Nothing]]
  */
protected[internals] class SimpleListValidationRuleStarterBuilder[A, B, +C](
  fieldsInformation: Vector[SingleFieldInformationContainer[A, B, C]],
  currentRuleStructure: Option[SimpleValidationRuleStructureContainer[A, B]],
  validationExpressions: Vector[SimpleValidationRuleStructureContainer[A, B]],
  markIndexesOfFieldNameErrors: Boolean,
):

  /** Generates a rule that will cause a validation error lazily evaluating the boolean expression returns true
    *
    * @param lazyBooleanValue Expression to apply
    */
  infix final def must(lazyBooleanValue: => Boolean): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val composedFunction = (_: B) => lazyBooleanValue
    addRuleExpressionToList(composedFunction)

  /** Generates a rule that will cause a validation error if applying this function to the property returns true
    *
    * @param ruleExpressionReceivingPropertyValue Expression to apply
    */
  infix final def must(
    ruleExpressionReceivingPropertyValue: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    addRuleExpressionToList(ruleExpressionReceivingPropertyValue)

  /** Generates a rule that will cause a validation error lazily evaluating the boolean expression returns true
    *
    * @param lazyBooleanValue Expression to apply
    */
  infix final def mustNot(lazyBooleanValue: => Boolean): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val composedFunction = (_: B) => lazyBooleanValue
    addRuleExpressionToList(composedFunction.andThen(!_))

  /** Generates a rule that will cause a validation error if applying this function to the property returns false
    *
    * @param ruleExpressionReceivingPropertyValue Expression to apply
    */
  infix final def mustNot(
    ruleExpressionReceivingPropertyValue: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    addRuleExpressionToList(ruleExpressionReceivingPropertyValue.andThen(!_))

  protected[internals] final def buildNextInstanceInChain(
    currentRuleStructure: Option[SimpleValidationRuleStructureContainer[A, B]],
    validationExpressions: Vector[SimpleValidationRuleStructureContainer[A, B]],
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    SimpleListValidationRuleContinuationBuilder(
      fieldsInformation,
      currentRuleStructure,
      validationExpressions,
      markIndexesOfFieldNameErrors,
    )

  private def addRuleExpressionToList(
    ruleExpression: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val ruleVector = currentRuleStructure match
      case None                => validationExpressions
      case Some(ruleStructure) => validationExpressions :+ ruleStructure
    buildNextInstanceInChain(
      Some(SimpleValidationRuleStructureContainer[A, B](ruleExpression, None, None, None, Vector.empty)),
      ruleVector,
    )
