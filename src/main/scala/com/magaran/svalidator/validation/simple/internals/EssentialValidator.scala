package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.validation.*

/** Base trait for all validators that permit a fluent style of validation
  *
  * @tparam Instance Type of objects to be validated
  * @tparam B Type of validation result returned by this validator
  */
protected[validation] trait EssentialValidator[Instance, B <: ValidationResult]
    extends Validator[Instance, B]
    with SharedRuleBuilders[Instance]:

  /** Returns a [[com.magaran.svalidator.validation.ResultWithoutData]] by applying the given
    * [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]]s to the given instance.
    *
    * @see [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleFor RuleFor]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForOptional RuleForOptional]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForEach RuleForEach]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForComponent RuleForComponent]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForOptionalComponent RuleForOptionalComponent]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForEachComponent RuleForEachComponent]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.For For]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.ForOptional ForOptional]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.ForEach ForEach]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.ForComponent ForComponent]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.ForOptionalComponent ForOptionalComponent]],
    *      [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.ForEachComponent ForEachComponent]]
    * @param ruleBuilders Builders to be applied to validate the instance
    * @param instance     Object to validate
    */
  protected final def WithRules(ruleBuilders: RuleBuilderWithMessageSet[Instance]*)(
    using instance: Instance
  ): ResultWithoutData =
    val results = applyRulesToInstance(ruleBuilders, instance)
    results match
      case Nil => Valid
      case nonEmptyList: ::[ValidationFailure] =>
        Invalid(nonEmptyList).withMetadata(nonEmptyList.map(_.metadata).fold(ValidationMetadata.empty)(_.merge(_)))

end EssentialValidator
