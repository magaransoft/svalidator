package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class FloatBinderSpecs extends Observes:

  private val fieldName                  = "someFloatFieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)
  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val sut: TypedBinder[Float]    = FloatBinder()

  describe("when binding a float value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherFloat") -> List("5.6"))
        val result           = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the float field with NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid float"):

        val invalidFieldValue = "aStringThatCanNotBeParsedAsFloat"
        given source: Source  = ValuesMap(fieldKey -> List(invalidFieldValue))
        val result            = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the float field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidFloatMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe("and the argument is present in the values map and is a valid float"):

        given source: Source = ValuesMap(fieldKey -> List("90.8"))

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(90.8f))

    describe("and the JsonCursor source method of binding is used"):

      describe("and the argument is not present in the json"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherFloat") -> Json.fromFloat(5.6f).get).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the float field with NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the json but its value is null"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the float field with NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid float"):

        val invalidFieldValue = "aStringThatCanNotBeParsedAsFloat"
        given source: Source  = JsonCursor(fieldKey -> Json.fromString(invalidFieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the float field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidFloatMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe("and the argument is present in the values map and is a valid float"):

        given source: Source = JsonCursor(fieldKey -> Json.fromFloat(90.8f).get).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(90.8f))
