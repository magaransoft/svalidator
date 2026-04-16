package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.validation.*
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

/** Chain builder for delegating component validation to a separate validator.
  *
  * @tparam Instance Type of the instance being validated
  * @tparam Component Type of the extracted component to delegate validation
  */
protected[internals] final class ComponentListValidationRuleBuilder[Instance, Component](
  componentListPropertyExpression: Instance => Seq[Component],
  fieldName: FieldKey,
  markIndexesOfFieldNameErrors: Boolean
):

  /** Returns a rule builder that will validate the extracted property component(s) using the passed in validator.
    *
    * @param validator Validator to delegate validation of the specified component(s)
    */
  infix def validateUsing(validator: Validator[Component, ?]): RuleBuilderWithMessageSet[Instance] =
    ComponentListValidationWrapper[Instance, Component](
      componentListPropertyExpression,
      fieldName,
      validator,
      markIndexesOfFieldNameErrors
    )

  /** Returns a rule builder that will validate the extracted property component(s) using the passed in functional validator.
    *
    * @param validator Validator to delegate validation of the specified component(s)
    */
  infix def validateUsing[InputData, Context](
    validator: MonadicValidator[Component, InputData, Context, ?]
  ): ComponentListFunctionalValidationRuleBuilder[Instance, Component, InputData, Context] = {
    ComponentListFunctionalValidationRuleBuilder(
      componentListPropertyExpression,
      fieldName,
      markIndexesOfFieldNameErrors,
      validator
    )
  }

protected[internals] final class ComponentListFunctionalValidationRuleBuilder[Instance, Component, InputData, Context](
  componentListPropertyExpression: Instance => Seq[Component],
  fieldName: FieldKey,
  markIndexesOfFieldNameErrors: Boolean,
  validator: MonadicValidator[Component, InputData, Context, ?]
) {

  infix def withInputData(inputData: InputData)(using Context): RuleBuilderWithMessageSet[Instance] = (_: Instance) => {
    RuleStreamCollection(
      Vector(
        ChainedValidationStream(
          Vector(
            LazyList(
              ComponentListFunctionalValidationRule(
                componentListPropertyExpression,
                fieldName,
                validator,
                inputData,
                markIndexesOfFieldNameErrors
              )
            )
          ),
          None
        )
      )
    )
  }
}

private class ComponentListValidationWrapper[A, B](
  componentListPropertyExpression: A => Seq[B],
  fieldName: FieldKey,
  componentValidator: Validator[B, ?],
  markIndexesOfFieldNameErrors: Boolean
) extends RuleBuilderWithMessageSet[A]:

  def buildRules(instance: A): RuleStreamCollection[A] =
    RuleStreamCollection(
      Vector(
        ChainedValidationStream(
          Vector(
            LazyList(
              ComponentListValidationRule[A, B](
                componentListPropertyExpression,
                fieldName,
                componentValidator,
                markIndexesOfFieldNameErrors
              )
            )
          ),
          None
        )
      )
    )

private class ComponentListValidationRule[A, B](
  componentListPropertyExpression: A => Seq[B],
  fieldKey: FieldKey,
  componentValidator: Validator[B, ?],
  markIndexesOfFieldErrors: Boolean
) extends ValidationRule[A]:

  def apply(instance: A): Vector[ValidationFailure] =
    val components = componentListPropertyExpression.apply(instance).toVector
    components.zipWithIndex.flatMap:
      case (component, index) =>
        val result: ValidationResult = componentValidator.validate(using component)
        result match
          case Valid    => Nil
          case Valid(_) => Nil
          case Invalid(validationFailures) =>
            ComponentFailuresToInstanceMapper.map(validationFailures, fieldKey, index, markIndexesOfFieldErrors)

private class ComponentListFunctionalValidationRule[Instance, Component, InputData, Context](
  componentListPropertyExpression: Instance => Seq[Component],
  fieldKey: FieldKey,
  componentValidator: MonadicValidator[Component, InputData, Context, ?],
  inputData: InputData,
  markIndexesOfFieldErrors: Boolean
)(using Context)
    extends ValidationRule[Instance]:

  def apply(instance: Instance): Vector[ValidationFailure] =
    val components = componentListPropertyExpression.apply(instance).toVector
    components.zipWithIndex.flatMap:
      case (component, index) =>
        val result = componentValidator.validate(component, inputData)
        result match
          case Right(_) => Nil
          case Left(Invalid(validationFailures)) =>
            ComponentFailuresToInstanceMapper.map(validationFailures, fieldKey, index, markIndexesOfFieldErrors)

private object ComponentFailuresToInstanceMapper {

  def map(
    validationFailures: ::[ValidationFailure],
    componentFieldKey: FieldKey,
    index: Int,
    markIndexesOfFieldErrors: Boolean
  ): List[ValidationFailure] = {
    componentFieldKey match
      case f: NestedFieldKey =>
        val keyToUse = if markIndexesOfFieldErrors then f.indexed(index) else f
        validationFailures.collect:
          case ValidationFailure(childFieldKey: NestedFieldKey, messageParts, metadata, alreadyLocalized) =>
            ValidationFailure(
              keyToUse.downField(childFieldKey.fullKey),
              messageParts,
              metadata,
              alreadyLocalized = alreadyLocalized
            )
          case failure => failure.copy(fieldKey = keyToUse)
      case FieldKey.Root => validationFailures
  }
}
