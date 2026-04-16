package com.magaran.svalidator.validation.simple.internals

import testUtils.asKey
import testUtils.Observes

class SingleFieldSimpleListValidationRuleStarterBuilderSpecs extends Observes:

  case class SampleValidatedClass(a: String, b: Long) {}

  describe("when building rules using the Simple Validation Rule Builder"):

    val instance = SampleValidatedClass("firstValue", 8L)

    describe("and build rules is called with a current rule structure that is not null"):
      val property_expression = stubUnCallableFunction[SampleValidatedClass, Vector[Long]]
      val rule_expression     = stubUnCallableFunction[Long, Boolean]
      val rule_structure_container =
        SimpleValidationRuleStructureContainer[SampleValidatedClass, Long](
          rule_expression,
          None,
          None,
          None,
          Vector.empty
        )

      val sut = SimpleListValidationRuleContinuationBuilder[SampleValidatedClass, Long, Nothing](
        Vector(
          SingleFieldInformationContainer(property_expression, "fieldName".asKey, None, None, FieldValidationChainId())
        ),
        Some(rule_structure_container),
        Vector.empty,
        false,
      )

      val result = sut.uncheckedBuildRules(instance)

      it("should return a list with as many rules as rule expressions passed in"):
        result.chains should have size 1
        result.chains.head.mainStream should have size 1

    /*
     * Other behavior of this class is tested on integration tests.
     * Testing more behavior as a unit test would imply making some members public which
     * is not desired at this time.
     */
