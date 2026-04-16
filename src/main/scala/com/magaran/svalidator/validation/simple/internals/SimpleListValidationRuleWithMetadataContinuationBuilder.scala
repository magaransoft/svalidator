package com.magaran.svalidator.validation.simple.internals

/** Continuation builder that extends a rule chain after metadata has been attached,
  * allowing further `must`/`when`/`withMessage` calls.
  */
protected[internals] final class SimpleListValidationRuleWithMetadataContinuationBuilder[A, B, +C](
  wrapped: SimpleListValidationRuleContinuationBuilder[A, B, C]
) extends ContinuationRuleBuilderWithMessageSet[A, B, C](wrapped)
