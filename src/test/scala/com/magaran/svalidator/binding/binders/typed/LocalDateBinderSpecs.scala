package com.magaran.svalidator.binding.binders.typed

import java.time.LocalDate

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class LocalDateBinderSpecs extends Observes:

  private val fieldName                   = "someDate"
  private val fieldKey                    = NestedFieldKey(fieldName)
  private given config: BindingConfig     = BindingConfig.default
  private given metadata: BindingContext  = BindingContext.empty
  private val sut: TypedBinder[LocalDate] = LocalDateBinder()

  describe("when binding a LocalDate value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherDate") -> List("2013-06-08"))
        val result           = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe(
        "and the argument is present in the values map with a value that is not a valid date in the expected format"
      ):
        val invalidFieldValue = "aStringThatCanNotBeParsedAsDate"

        given source: Source = ValuesMap(fieldKey -> List(invalidFieldValue))
        val result           = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the field with the proper message"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidDateMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe(
        "and the argument is present in the values map with a valid date in the expected format (yyyy-MM-dd by default)"
      ):

        val dateString = "2013-02-14"

        given source: Source = ValuesMap(fieldKey -> List(dateString))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to the parsed date"):
          result should equal(BindingPass(LocalDate.of(2013, 2, 14)))

    describe("and the JsonCursor source method of binding is used"):

      describe("and the argument is not present in the json"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherDate") -> Json.fromString("2013-06-08")).downField(fieldName)

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
        "and the argument is present in the values map with a value that is not a valid date in the expected format"
      ):
        val invalidFieldValue = "aStringThatCanNotBeParsedAsDate"

        given source: Source = JsonCursor(fieldKey -> Json.fromString(invalidFieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidDateMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe(
        "and the argument is present in the values map with a valid date in the expected format (yyyy-MM-dd by default)"
      ):

        val dateString = "2013-02-14"

        given source: Source = JsonCursor(fieldKey -> Json.fromString(dateString)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to the parsed date"):
          result should equal(BindingPass(LocalDate.of(2013, 2, 14)))
