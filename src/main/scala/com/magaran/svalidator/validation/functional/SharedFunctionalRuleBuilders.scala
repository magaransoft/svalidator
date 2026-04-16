package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.simple.internals.SharedRuleBuilders

/** Mixin providing the `WhenValidating` entry point for functional-style validators.
  * Extends [[com.magaran.svalidator.validation.simple.internals.SharedRuleBuilders]] to inherit the `For` rule builder DSL.
  *
  * @tparam Instance the type of the instance being validated
  */
trait SharedFunctionalRuleBuilders[Instance] extends SharedRuleBuilders[Instance] {

  protected final def WhenValidating(instance: Instance): WhenValidatingBuilder[Instance] =
    new WhenValidatingBuilder(instance, applyRulesToInstance)

}
