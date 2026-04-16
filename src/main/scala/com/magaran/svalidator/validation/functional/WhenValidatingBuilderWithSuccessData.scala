package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet
import com.magaran.svalidator.validation.Invalid
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.ValidationFailure
import com.magaran.svalidator.validation.ValidationMetadata

protected[functional] final class WhenValidatingBuilderWithSuccessData[Instance, SuccessData](
  instance: Instance,
  successData: => SuccessData,
  applyRulesToInstance: (Seq[RuleBuilderWithMessageSet[Instance]], Instance) => List[ValidationFailure]
) {

  /** Returns an [[scala.Either Either]] by applying the passed in
    * [[com.magaran.svalidator.validation.functional.internals.RuleBuilderWithMessageSet RuleBuilderWithMessageSet]]s to the target instance.
    *
    * @see [[com.magaran.svalidator.validation.functional.FunctionalValidator.For For]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.ForOptional ForOptional]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.ForEach ForEach]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.ForComponent ForComponent]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.ForOptionalComponent ForOptionalComponent]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.ForEachComponent ForEachComponent]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.RuleFor RuleFor]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.RuleForOptional RuleForOptional]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.RuleForEach RuleForEach]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.RuleForComponent RuleForComponent]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.RuleForOptionalComponent RuleForOptionalComponent]],
    *      [[com.magaran.svalidator.validation.functional.FunctionalValidator.RuleForEachComponent RuleForEachComponent]],
    * @param ruleBuilders Builders to be applied to validate the instance
    */
  def withRules(ruleBuilders: RuleBuilderWithMessageSet[Instance]*)(
    using localizer: Localizer = Localizer.NoLocalization
  ): Either[Invalid, SuccessData] =
    val results = applyRulesToInstance(ruleBuilders, instance)
    results match
      case Nil => Right(successData)
      case nonEmptyList: ::[ValidationFailure] =>
        val invalid =
          Invalid(nonEmptyList).localize.withMetadata(
            nonEmptyList.map(_.metadata).fold(ValidationMetadata.empty)(_.merge(_))
          )
        Left(invalid)

}
