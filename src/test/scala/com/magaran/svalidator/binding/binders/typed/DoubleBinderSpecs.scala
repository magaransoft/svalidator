package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class DoubleBinderSpecs extends Observes:

  private val fieldName                  = "someDoubleFieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)
  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val languageConfig             = BindingConfig.default.languageConfig
  private val sut: TypedBinder[Double]   = DoubleBinder()

  describe("when binding a double value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherDouble") -> List("8.8"))
        val result           = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the double field with NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.noValueProvidedMessage(fieldKey))
          result.asInstanceOf[BindingFailure].cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid double"):

        val invalidFieldValue = "aStringThatCanNotBeParsedAsDouble"

        given source: Source = ValuesMap(fieldKey -> List(invalidFieldValue))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the double field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.invalidDoubleMessage(fieldKey, invalidFieldValue.quoted))

      describe("and the argument is present in the values map and is a valid double"):

        given source: Source = ValuesMap(fieldKey -> List("170.5"))
        val result           = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(170.5d))

    describe("and the json method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the double field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.noValueProvidedMessage(fieldKey))
          result.asInstanceOf[BindingFailure].cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present with a value of null"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherDouble") -> Json.fromDouble(8.8d).get).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the double field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.noValueProvidedMessage(fieldKey))
          result.asInstanceOf[BindingFailure].cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid double"):

        val invalidFieldValue = "aStringThatCanNotBeParsedAsDouble"
        given source: Source  = JsonCursor(fieldKey -> Json.fromString(invalidFieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the double field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.invalidDoubleMessage(fieldKey, invalidFieldValue.quoted))

      describe("and the argument is present in the values map and is a valid double"):

        given source: Source = JsonCursor(fieldKey -> Json.fromDouble(170.5d).get).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(170.5d))
