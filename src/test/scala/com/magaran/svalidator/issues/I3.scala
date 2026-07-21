package com.magaran.svalidator.issues

import com.magaran.svalidator.validation.Invalid
import com.magaran.svalidator.validation.MonadicValidator
import com.magaran.svalidator.validation.functional.ContextualValidator
import testUtils.Observes

/** Regression for
  * [[https://github.com/magaransoft/svalidator/issues/3 issue #3]]:
  * `MonadicValidator` is contravariant in `Instance`, matching the
  * OO-style `Validator[-A, +B]` — a validator declared for a parent
  * type is a validator for every child, so one family validator
  * (`ContextualValidator[ParentCommand, Ctx]`) serves each child
  * command. This is the shape framework integrations rely on
  * (sky-core's `SkyValidatorAdapter` blanket-adapter search binds the
  * instance type to the exact command). The `FunctionalValidator`
  * family itself stays invariant — its rule-builder surface returns
  * `Instance`-parameterized builders — which is sufficient: the
  * subtyping chain only needs the top trait.
  */
class I3 extends Observes:

  sealed trait ParentCommand:
    def name: String

  case class CreateCommand(name: String) extends ParentCommand
  case class UpdateCommand(name: String) extends ParentCommand

  class FamilyValidator extends ContextualValidator[ParentCommand, Unit]:
    var lastValidatedName: Option[String] = None

    def validate(instance: ParentCommand)(using Unit): Either[Invalid, Unit] =
      lastValidatedName = Some(instance.name)
      Right(())

  describe("MonadicValidator contravariance (issue #3)"):

    it("a parent-typed validator IS a MonadicValidator of every child — compile-time evidence"):
      val parentServesCreate =
        summon[MonadicValidator[ParentCommand, Unit, Unit, Unit] <:< MonadicValidator[CreateCommand, Unit, Unit, Unit]]
      val parentServesUpdate =
        summon[MonadicValidator[ParentCommand, Unit, Unit, Unit] <:< MonadicValidator[UpdateCommand, Unit, Unit, Unit]]
      // the summons above ARE the assertions — they fail to compile
      // under an invariant MonadicValidator
      val _ = (parentServesCreate, parentServesUpdate)

    it("dispatches a child instance through the parent-typed validator"):
      val familyValidator = new FamilyValidator
      val forCreates: MonadicValidator[CreateCommand, Unit, Unit, Unit] = familyValidator

      val result = forCreates.validate(CreateCommand("gizmo"), ())(using ())

      val _ = result shouldBe Right(())
      val _ = familyValidator.lastValidatedName shouldBe Some("gizmo")
