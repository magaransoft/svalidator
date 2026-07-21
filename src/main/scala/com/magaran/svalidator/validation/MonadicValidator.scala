package com.magaran.svalidator.validation

/** Functional style variant of the [[com.magaran.svalidator.validation.Validator Validator]] trait
  *
  * `Instance` is contravariant (issue #3), matching the OO-style
  * `Validator[-A, +B]`: a validator declared for a parent type is a
  * validator for every child, so one family validator
  * (`ContextualValidator[ParentCommand, Ctx]`) serves each child
  * command — the shape framework integrations (sky-core's
  * `SkyValidatorAdapter` search) rely on. Sound because `Instance`
  * only ever appears as `validate`'s parameter. The
  * [[com.magaran.svalidator.validation.functional.FunctionalValidator FunctionalValidator]]
  * family stays invariant — its rule-builder surface returns
  * `Instance`-parameterized builders — which is sufficient: the
  * subtyping chain only needs this top trait
  * (`FamilyValidator <: MonadicValidator[Parent, ...] <:
  * MonadicValidator[Child, ...]`).
  *
  * @tparam Instance Type of objects to be validated (contravariant)
  */
trait MonadicValidator[-Instance, InputData, Context, SuccessData] {

  /** Returns a [[com.magaran.svalidator.validation.ValidationResult]] with error information
    * from validating the instance
    *
    * @param instance Instance to validate
    */
  def validate(instance: Instance, inputData: InputData)(using Context): Either[Invalid, SuccessData]

}
