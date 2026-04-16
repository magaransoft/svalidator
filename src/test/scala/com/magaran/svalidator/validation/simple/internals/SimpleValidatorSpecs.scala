package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.validation.*
import com.magaran.svalidator.validation.simple.SimpleValidator
import org.mockito.ArgumentMatchers.any
import testUtils.asKey
import testUtils.wasNeverToldTo
import testUtils.Observes

class SimpleValidatorSpecs extends Observes:

  private val rule_builder_1 = mock[RuleBuilderWithMessageSet[SampleValidatedClass]]
  private val rule_builder_2 = mock[RuleBuilderWithMessageSet[SampleValidatedClass]]
  private val rule_builder_3 = mock[RuleBuilderWithMessageSet[SampleValidatedClass]]
  private val rule_builder_4 = mock[RuleBuilderWithMessageSet[SampleValidatedClass]]

  case class SampleValidatedClass(a: String, b: Long) {}

  class SampleSimpleValidator extends SimpleValidator[SampleValidatedClass]:
    def validate(using SampleValidatedClass): ResultWithoutData =
      WithRules(rule_builder_1, rule_builder_2, rule_builder_3, rule_builder_4)

  describe("when performing validation assisted by an instance of a child class of simple validator"):

    val sut: Validator[SampleValidatedClass, ResultWithoutData] = SampleSimpleValidator()

    val instance = mock[SampleValidatedClass]

    val rule_1 = mock[ValidationRule[SampleValidatedClass]]
    val rule_2 = mock[ValidationRule[SampleValidatedClass]]
    val rule_3 = mock[ValidationRule[SampleValidatedClass]]
    val rule_list_1 =
      RuleStreamCollection(Vector(new ChainedValidationStream(Vector(LazyList(rule_1, rule_2, rule_3)), None)))

    val rule_4 = mock[ValidationRule[SampleValidatedClass]]
    val rule_5 = mock[ValidationRule[SampleValidatedClass]]
    val rule_6 = mock[ValidationRule[SampleValidatedClass]]
    val rule_list_2 =
      RuleStreamCollection(Vector(new ChainedValidationStream(Vector(LazyList(rule_4, rule_5, rule_6)), None)))

    val rule_7 = mock[ValidationRule[SampleValidatedClass]]
    val rule_8 = mock[ValidationRule[SampleValidatedClass]]
    val rule_9 = mock[ValidationRule[SampleValidatedClass]]
    val rule_list_3 =
      RuleStreamCollection(Vector(new ChainedValidationStream(Vector(LazyList(rule_7, rule_8, rule_9)), None)))

    val rule_10 = mock[ValidationRule[SampleValidatedClass]]
    val rule_11 = mock[ValidationRule[SampleValidatedClass]]
    val rule_12 = mock[ValidationRule[SampleValidatedClass]]
    val rule_list_4 =
      RuleStreamCollection(Vector(new ChainedValidationStream(Vector(LazyList(rule_10, rule_11, rule_12)), None)))

    when(rule_builder_1.buildRules(instance)).thenReturn(rule_list_1)
    when(rule_1.apply(instance)).thenReturn(Vector.empty)
    when(rule_2.apply(instance)).thenReturn(Vector.empty)
    when(rule_3.apply(instance)).thenReturn(Vector.empty)

    when(rule_builder_2.buildRules(instance)).thenReturn(rule_list_2)
    when(rule_builder_3.buildRules(instance)).thenReturn(rule_list_3)
    when(rule_builder_4.buildRules(instance)).thenReturn(rule_list_4)

    describe("and some of the rule sets return validation failures"):

      val failure_1 =
        ValidationFailure("fieldNameInSet2".asKey, MessageParts("errorMessageInRule4"), ValidationMetadata.empty)
      val failure_2 =
        ValidationFailure("fieldNameInSet3".asKey, MessageParts("errorMessageInRule8"), ValidationMetadata.empty)
      val failure_3 =
        ValidationFailure("fieldNameInSet4".asKey, MessageParts("errorMessageInRule12"), ValidationMetadata.empty)
      val failure_4 = ValidationFailure(
        "fieldNameInSet5".asKey,
        MessageParts("errorMessageInRule8 second time"),
        ValidationMetadata.empty
      )

      when(rule_4.apply(instance)).thenReturn(Vector(failure_1))

      when(rule_7.apply(instance)).thenReturn(Vector.empty)
      when(rule_8.apply(instance)).thenReturn(Vector(failure_2, failure_4))

      when(rule_10.apply(instance)).thenReturn(Vector.empty)
      when(rule_11.apply(instance)).thenReturn(Vector.empty)
      when(rule_12.apply(instance)).thenReturn(Vector(failure_3))

      lazy val result = sut.validate(using instance)

      it("return the first validation failure in each rule list"):
        result.asInstanceOf[Invalid].validationFailures should equal(Vector(failure_1, failure_2, failure_4, failure_3))

      it("should have applied any rules in the lists after the first validation failure"):
        rule_5.wasNeverToldTo(_.apply(any[SampleValidatedClass]))
        rule_6.wasNeverToldTo(_.apply(any[SampleValidatedClass]))
        rule_9.wasNeverToldTo(_.apply(any[SampleValidatedClass]))

    describe("and none of the rule sets return validation failures"):

      when(rule_4.apply(instance)).thenReturn(Vector.empty)
      when(rule_5.apply(instance)).thenReturn(Vector.empty)
      when(rule_6.apply(instance)).thenReturn(Vector.empty)

      when(rule_7.apply(instance)).thenReturn(Vector.empty)
      when(rule_8.apply(instance)).thenReturn(Vector.empty)
      when(rule_9.apply(instance)).thenReturn(Vector.empty)

      when(rule_10.apply(instance)).thenReturn(Vector.empty)
      when(rule_11.apply(instance)).thenReturn(Vector.empty)
      when(rule_12.apply(instance)).thenReturn(Vector.empty)

      lazy val result = sut.validate(using instance)

      it("return no validation failures"):
        result shouldEqual Valid
