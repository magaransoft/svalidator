package com.magaran.svalidator.validation.simple.internals

/** Base trait for the result of all constructs that permit a fluent style of validation
  *
  * @tparam A Type of object being validated
  */
protected[internals] trait RuleBuilderWithoutMessageSet[-A]:

  /** This method is named unchecked because the caller MUST ENSURE that the message is set
    * in the rule that is being built.  If the message is not set, calling this method will throw an
    * IllegalStateException.
    * @param instance The instance to build rules upon
    */
  protected[internals] def uncheckedBuildRules(instance: A): RuleStreamCollection[A]
