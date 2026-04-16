package com.magaran.svalidator.validation.simple.internals

/** Chain builder that only executes all its internal builders if the passed in condition evaluates to true
  *
  * @param conditionalExpression Condition to apply to the instance
  * @tparam A Type of the instance being validated
  */
protected[internals] final class ConditionedGroupValidationRuleBuilder[A](conditionalExpression: A => Boolean):

  /** Returns a builder containing all the passed in ruleBuilders that will only be executed if the conditional expression
    * evaluates to true
    *
    * @param ruleBuilder Rule builders that will be conditioned
    */
  def apply(ruleBuilder: RuleBuilderWithMessageSet[A]*): RuleBuilderWithMessageSet[A] =
    ConditionedGroupValidationRuleBuilderWrapper[A](conditionalExpression, ruleBuilder.toVector)

private class ConditionedGroupValidationRuleBuilderWrapper[A](
  conditionalExpression: A => Boolean,
  ruleBuilders: Vector[RuleBuilderWithMessageSet[A]]
) extends RuleBuilderWithMessageSet[A]:

  def buildRules(instance: A): RuleStreamCollection[A] =
    if conditionalExpression(instance) then
      val ruleStreamCollections = ruleBuilders.map(_.buildRules(instance))
      RuleStreamCollection(ruleStreamCollections.flatMap(_.chains))
    else RuleStreamCollection.Empty
