package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class IntBinderSpecs extends Observes:

  private val fieldName                  = "someIntFieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)
  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val sut: TypedBinder[Int]      = IntBinder()

  describe("when binding an int value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherInt") -> List("5"))

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the int field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid int"):

        val invalidFieldValue = "98.5"
        given source: Source  = ValuesMap(fieldKey -> List(invalidFieldValue))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the int field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidIntegerMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe("and the argument is present in the values map and is a valid int"):

        given source: Source = ValuesMap(fieldKey -> List("18"))

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(18))

    describe("and the json method of binding is used"):

      describe("and the argument is not present in the json"):

        given source: Source = JsonCursor(NestedFieldKey("someOtherInt") -> Json.fromInt(5)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the int field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the json with a value of null"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the int field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the json but it is not a valid int"):

        val invalidFieldValue = 98.5d

        given source: Source =
          JsonCursor(fieldKey -> Json.fromDouble(invalidFieldValue).get).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the int field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig
              .invalidIntegerMessage(fieldKey, invalidFieldValue.toString.quoted)
          )

      describe("and the argument is present in the json and is a valid int"):

        val validValue = 18

        given source: Source = JsonCursor(fieldKey -> Json.fromInt(validValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(validValue))
