package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet
import com.magaran.svalidator.validation.ValidationFailure

/** Intermediate builder created by `WhenValidating(instance)` in functional validators.
  * Call `withSuccessData` to specify the value returned on successful validation, then
  * `withRules` to define the validation rules.
  *
  * @tparam A the type of the instance being validated
  */
protected[functional] final class WhenValidatingBuilder[A](
  instance: A,
  applyRulesToInstance: (Seq[RuleBuilderWithMessageSet[A]], A) => List[ValidationFailure]
) {

  def withSuccessData[B](data: => B): WhenValidatingBuilderWithSuccessData[A, B] =
    new WhenValidatingBuilderWithSuccessData(instance, data, applyRulesToInstance)
}
