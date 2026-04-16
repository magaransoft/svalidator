package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingFailure
import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.binding.JsonCursor
import com.magaran.svalidator.binding.Source
import com.magaran.svalidator.binding.ValuesMap
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class LongBinderSpecs extends Observes:

  private val fieldName                  = "someLong"
  private val fieldKey                   = NestedFieldKey(fieldName)
  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val sut: TypedBinder[Long]     = LongBinder()
  private val languageConfig             = BindingConfig.default.languageConfig

  describe("when binding a long value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherLong") -> List("5"))

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the long field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid long"):

        val invalidFieldValue = "98.5"
        given source: Source  = ValuesMap(fieldKey -> List(invalidFieldValue))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the long field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.invalidLongMessage(fieldKey, invalidFieldValue.quoted))

      describe("and the argument is present in the values map and is a valid long"):

        given source: Source = ValuesMap(fieldKey -> List("18"))

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(18L))

    describe("and the JsonCursor source method of binding is used"):

      describe("and the argument is not present in the source"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherLong") -> Json.fromLong(5)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the long field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present with a value of null"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the long field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the source but it is not a valid long"):

        val invalidFieldValue = 98.5d

        given source: Source =
          JsonCursor(fieldKey -> Json.fromDouble(invalidFieldValue).get).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the long field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            languageConfig.invalidLongMessage(fieldKey, invalidFieldValue.toString.quoted)
          )

      describe("and the argument is present in the source and is a valid long"):

        val validValue = 18L

        given source: Source = JsonCursor(fieldKey -> Json.fromLong(validValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(validValue))
