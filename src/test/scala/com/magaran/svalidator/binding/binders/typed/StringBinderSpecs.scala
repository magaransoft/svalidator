package com.magaran.svalidator.binding.binders.typed

import java.time.format.DateTimeFormatter

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.config.BooleanValuesMapBindingStyle
import com.magaran.svalidator.config.StringBindingStyle
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class StringBinderSpecs extends Observes:

  private val fieldName                    = "someStringFieldName"
  private val fieldKey                     = NestedFieldKey(fieldName)
  private given metadata: BindingContext   = BindingContext.empty
  private val sut: TypedBinder[String]     = StringBinder()
  private val defaultConfig: BindingConfig = BindingConfig.default

  StringBindingStyle.values.foreach { style =>

    given config: BindingConfig = new BindingConfig:
      val dateFormatter: DateTimeFormatter                           = defaultConfig.dateFormatter
      val timeFormatter: DateTimeFormatter                           = defaultConfig.timeFormatter
      val stringBindingStyle: StringBindingStyle                     = style
      val languageConfig: BindingLanguageConfig                      = defaultConfig.languageConfig
      val booleanValuesMapBindingStyle: BooleanValuesMapBindingStyle = defaultConfig.booleanValuesMapBindingStyle

    style match
      case StringBindingStyle.InvalidateFullWhitespacePreserveWhitespaces =>
        describe(
          s"when binding a String with style set to ${StringBindingStyle.InvalidateFullWhitespacePreserveWhitespaces}"
        ):

          val whiteSpaceString = "             "
          describe("and the source method of binding is used"):

            describe("and the field is not present in the source"):

              given source: Source = ValuesMap(NestedFieldKey("aDifferentField") -> List("someValue"))

              val result = sut.bind(source, fieldKey)

              it("should have returned a failure for the field with NoSuchElementException as the cause"):
                val casted = result.asInstanceOf[BindingFailure]
                casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                val error = casted.fieldErrors.head
                error.messageParts should equal(config.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted))
                casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

            describe("and the field is present in the source"):

              describe("and the field is an empty string"):

                given source: Source = ValuesMap(fieldKey -> List(""))

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    config.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the field is a whitespace string"):

                given source: Source = ValuesMap(fieldKey -> List(whiteSpaceString))
                val result           = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    config.languageConfig.invalidNonEmptyStringMessage(fieldKey, whiteSpaceString.quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the valueGetter is a non-whitespace string with spaces on the edges"):

                val fieldValue       = " someValue "
                given source: Source = ValuesMap(fieldKey -> List(fieldValue))

                val result = sut.bind(source, fieldKey)

                it("should have bound the property including its spaces properly"):
                  result should equal(BindingPass(fieldValue))

          describe("and the JsonCursor source method of binding is used"):

            describe("and the field is not present in the source"):

              given source: Source =
                JsonCursor(NestedFieldKey("aDifferentField") -> Json.fromString("someValue")).downField(fieldName)

              val result = sut.bind(source, fieldKey)

              it("should have returned a failure for the field with NoSuchElementException as the cause"):
                val casted = result.asInstanceOf[BindingFailure]
                casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                val error = casted.fieldErrors.head
                error.messageParts should equal(config.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted))
                casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

            describe("and the field is present in the source"):

              describe("and the field is null"):

                given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    config.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                  )
                  casted.cause.isEmpty shouldEqual true

              describe("and the field is an empty string"):

                given source: Source = JsonCursor(fieldKey -> Json.fromString("")).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    config.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the field is a whitespace string"):

                given source: Source = JsonCursor(fieldKey -> Json.fromString(whiteSpaceString)).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have returned a Binding Failure with a field error for the string field"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    config.languageConfig.invalidNonEmptyStringMessage(fieldKey, whiteSpaceString.quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the valueGetter is a non-whitespace string with spaces on the edges"):

                val fieldValue = " someValue "

                given source: Source = JsonCursor(fieldKey -> Json.fromString(fieldValue)).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have bound the property including its spaces properly"):
                  result should equal(BindingPass(fieldValue))
      case StringBindingStyle.TrimWhitespaceInvalidateEmpty =>
        describe(s"when binding a String with style set to ${StringBindingStyle.TrimWhitespaceInvalidateEmpty}"):

          val whiteSpaceString = "             "
          describe("and the source method of binding is used"):

            describe("and the field is not present in the source"):

              given source: Source = ValuesMap(NestedFieldKey("aDifferentField") -> List("someValue"))

              val result = sut.bind(source, fieldKey)

              it("should have returned a failure for the field with NoSuchElementException as the cause"):
                val casted = result.asInstanceOf[BindingFailure]
                casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                val error = casted.fieldErrors.head
                error.messageParts should equal(config.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted))
                casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

            describe("and the field is present in the source"):

              describe("and the field is an empty string"):

                given source: Source = ValuesMap(fieldKey -> List(""))

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    BindingConfig.default.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the field is a whitespace string"):

                given source: Source = ValuesMap(fieldKey -> List(whiteSpaceString))

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    BindingConfig.default.languageConfig.invalidNonEmptyStringMessage(fieldKey, whiteSpaceString.quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the valueGetter is a non-whitespace string with spaces on the edges"):

                val fieldValue = " someValue "

                given source: Source = ValuesMap(fieldKey -> List(fieldValue))

                val result = sut.bind(source, fieldKey)

                it("should have bound the property but trimmed edge spaces"):
                  result should equal(BindingPass(fieldValue.trim))

          describe("and the JsonCursor source method of binding is used"):

            describe("and the field is not present in the source"):

              given source: Source =
                JsonCursor(NestedFieldKey("aDifferentField") -> Json.fromString("someValue")).downField(fieldName)

              val result = sut.bind(source, fieldKey)

              it("should have returned a failure for the field with NoSuchElementException as the cause"):
                val casted = result.asInstanceOf[BindingFailure]
                casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                val error = casted.fieldErrors.head
                error.messageParts should equal(
                  BindingConfig.default.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                )
                casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

            describe("and the field is present in the source"):

              describe("and the field is null"):

                given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    BindingConfig.default.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                  )
                  casted.cause.isEmpty shouldEqual true

              describe("and the field is an empty string"):

                given source: Source = JsonCursor(fieldKey -> Json.fromString("")).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have returned a failure for the field with NoSuchElementException as the cause"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    BindingConfig.default.languageConfig.invalidNonEmptyStringMessage(fieldKey, "".quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the field is a whitespace string"):

                given source: Source = JsonCursor(fieldKey -> Json.fromString(whiteSpaceString)).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have returned a Binding Failure with a field error for the string field"):
                  val casted = result.asInstanceOf[BindingFailure]
                  casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
                  val error = casted.fieldErrors.head
                  error.messageParts should equal(
                    BindingConfig.default.languageConfig.invalidNonEmptyStringMessage(fieldKey, whiteSpaceString.quoted)
                  )
                  casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

              describe("and the valueGetter is a non-whitespace string with spaces on the edges"):

                val fieldValue = " someValue "

                given source: Source = JsonCursor(fieldKey -> Json.fromString(fieldValue)).downField(fieldName)

                val result = sut.bind(source, fieldKey)

                it("should have bound the property trimming edge spaces"):
                  result should equal(BindingPass(fieldValue.trim))
  }
