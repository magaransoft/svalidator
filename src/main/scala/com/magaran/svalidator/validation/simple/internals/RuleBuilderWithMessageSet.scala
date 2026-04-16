package com.magaran.svalidator.validation.simple.internals

/** Base trait for the result of all constructs that permit a fluent style of validation
  *
  * @tparam A Type of object being validated
  */
protected[validation] trait RuleBuilderWithMessageSet[-A]:

  protected[validation] def buildRules(instance: A): RuleStreamCollection[A]
