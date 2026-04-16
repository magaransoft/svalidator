package com.magaran.svalidator.validation.simple.internals

import testUtils.asKey
import testUtils.Observes

class SimpleValidationRuleSpecs extends Observes:

  describe("when using a simple validation rule to validate a specific property on an object with a given field name"):
    val fieldKey = "someFieldNameHere".asKey
    val instance = TestClass("someName", 18, single = true)

    describe("and the conditioned validation expression returns false"):

      val conditioned_validation             = stubFunction(instance.age, instance, false)
      lazy val property_extractor_expression = List(18L)
      val rule_expression                    = stubUnCallableFunction[Long, Boolean]

      val sut: ValidationRule[TestClass] = SingleFieldSimpleListValidationRule(
        property_extractor_expression,
        fieldKey,
        rule_expression,
        "error.key",
        None,
        conditioned_validation,
        false,
        Vector.empty
      )

      val result = sut.apply(instance)

      it("should have returned an empty list as the validation result"):
        result should equal(Nil)

    describe("and the conditioned validation expression returns true"):

      val some_property_value = 4935L

      val conditioned_validation         = stubFunction(some_property_value, instance, true)
      lazy val property_value_expression = List(some_property_value)

      describe("and the rule expression returns true"):
        val rule_expression = stubFunction(some_property_value, true)

        val sut: ValidationRule[TestClass] = SingleFieldSimpleListValidationRule(
          property_value_expression,
          fieldKey,
          rule_expression,
          "error.key",
          None,
          conditioned_validation,
          false,
          Vector.empty
        )

        val result = sut.apply(instance)

        it("should have returned an empty list as the validation result"):
          result should equal(Nil)

      describe("and the rule expression returns false"):

        val rule_expression = stubFunction(some_property_value, false)

        val sut: ValidationRule[TestClass] = SingleFieldSimpleListValidationRule(
          property_value_expression,
          fieldKey,
          rule_expression,
          "error.key",
          None,
          conditioned_validation,
          false,
          Vector.empty
        )

        val result = sut.apply(instance)

        it("should have returned a non empty list containing a single validation failure"):
          result should have size 1

        it("should have set the field name to the passed in field name valueGetter"):
          val resultFailure = result.head
          resultFailure.fieldKey should equal(fieldKey)

        it("should have set the error message to the valueGetter generated using the field name and valueGetter"):
          val resultFailure = result.head
          resultFailure.message should equal("error.key")

  case class TestClass(name: String, age: Long, single: Boolean) {}
