package com.magaran.svalidator.validation.simple.internals

import org.jetbrains.annotations.PropertyKey

protected[internals] final class SimpleListValidationRuleWhenContinuationBuilder[A, B, +C](
  wrapped: SimpleListValidationRuleContinuationBuilder[A, B, C]
) extends RuleBuilderWithoutMessageSet[A]
    with UpstreamLazyValueProvider[Vector[B]]:

  /** Assigns the messageKey for the preceding [[must]] or [[mustNot]] call.
    *
    * By default, the message will use the value of the property when it is formatted using
    * [[scala.collection.StringOps.format StringOps.format]].  If you'd like to use a custom list of values,
    * call [[SimpleListValidationRuleWithMessageContinuationBuilder.withFormat(value:* withFormat]] in this chain.
    *
    * @param messageKey   The raw message or a key string for localized messages
    */
  infix def withMessage(
    @PropertyKey(resourceBundle = "messages") messageKey: String
  ): SimpleListValidationRuleWithMessageContinuationBuilder[A, B, C] =
    wrapped.withMessage(messageKey)

  protected[internals] override def uncheckedBuildRules(instance: A): RuleStreamCollection[A] =
    wrapped.uncheckedBuildRules(instance)

  protected[internals] override def fetchValue(chainId: FieldValidationChainId): Vector[B] = wrapped.fetchValue(chainId)
