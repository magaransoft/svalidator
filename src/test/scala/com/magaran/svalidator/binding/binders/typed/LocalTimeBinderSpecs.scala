package com.magaran.svalidator.binding.binders.typed

import java.time.LocalTime

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class LocalTimeBinderSpecs extends Observes:

  private val fieldName                   = "someTime"
  private val fieldKey                    = NestedFieldKey(fieldName)
  private given config: BindingConfig     = BindingConfig.default
  private given metadata: BindingContext  = BindingContext.empty
  private val sut: TypedBinder[LocalTime] = LocalTimeBinder()

  describe("when binding a local time value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):
        given source: Source = ValuesMap(NestedFieldKey("someOtherTime") -> List("11:00 PM"))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe(
        "and the argument is present in the values map with a value that is not a valid time in the expected format"
      ):
        val invalidFieldValue = "aStringThatCanNotBeParsedAsTime"

        given source: Source = ValuesMap(fieldKey -> List(invalidFieldValue))
        val result           = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidTimeMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe(
        "and the argument is present in the values map with a valid time in the expected format (HH:mm by default)"
      ):
        val timeString       = "20:17"
        given source: Source = ValuesMap(fieldKey -> List(timeString))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to the parsed time"):
          result should equal(BindingPass(LocalTime.of(20, 17)))

    describe("and the JsonCursor method of binding is used"):

      describe("and the target field is not present"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherTime") -> Json.fromString("11:00 PM")).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          result.asInstanceOf[BindingFailure].cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present with a value of null"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          result.asInstanceOf[BindingFailure].cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe(
        "and the argument is present in the values map with a value that is not a valid time in the expected format"
      ):
        val invalidFieldValue = "aStringThatCanNotBeParsedAsTime"

        given source: Source = JsonCursor(fieldKey -> Json.fromString(invalidFieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidTimeMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe(
        "and the argument is present in the values map with a valid time in the expected format (HH:mm by default)"
      ):

        val timeString       = "20:17"
        given source: Source = JsonCursor(fieldKey -> Json.fromString(timeString)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to the parsed time"):
          result should equal(BindingPass(LocalTime.of(20, 17)))
