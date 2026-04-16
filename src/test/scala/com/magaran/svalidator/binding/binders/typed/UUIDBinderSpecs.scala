package com.magaran.svalidator.binding.binders.typed

import java.util.UUID

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class UUIDBinderSpecs extends Observes:

  private val fieldName                  = "someUUIDFieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)
  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val sut: TypedBinder[UUID]     = UUIDBinder()

  describe("when binding a UUID value"):

    val whiteSpaceUUID = "             "
    describe("and the source method of binding is used"):

      describe("and the field is not present in the source"):

        given source: Source = ValuesMap(NestedFieldKey("aDifferentField") -> List("someValue"))

        val result = sut.bind(source, fieldKey)

        it("should have returned a failure for the field with NoSuchElementException as the cause"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(config.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the field is present in the source"):

        describe("and the field is an empty string"):

          given source: Source = ValuesMap(fieldKey -> List(""))

          val result = sut.bind(source, fieldKey)

          it("should have returned a failure for the field with NoSuchElementException as the cause"):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

        describe("and the field is a whitespace string"):

          given source: Source = ValuesMap(fieldKey -> List(whiteSpaceUUID))
          val result           = sut.bind(source, fieldKey)

          it("should have returned a failure for the field with NoSuchElementException as the cause"):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

        describe("and the field is not a valid UUID"):

          val fieldValue       = "someValue"
          given source: Source = ValuesMap(fieldKey -> List(fieldValue))
          val result           = sut.bind(source, fieldKey)

          it("should have returned a failure with with any exception that is not NoSuchElementException as the cause"):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(
              BindingConfig.default.languageConfig.invalidUUIDMessage(fieldKey, fieldValue.quoted)
            )
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(false)

        describe("and the value is a valid UUID"):
          val uuid             = UUID.randomUUID()
          val fieldValue       = uuid.toString
          given source: Source = ValuesMap(fieldKey -> List(fieldValue))

          val result = sut.bind(source, fieldKey)

          it("should have bound the property including its spaces properly"):
            result should equal(BindingPass(uuid))

    describe("and the JsonCursor source method of binding is used"):

      describe("and the field is not present in the source"):

        given source: Source =
          JsonCursor(NestedFieldKey("aDifferentField") -> Json.fromString("someValue")).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a failure for the field with NoSuchElementException as the cause"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the field is present in the source"):

        describe("and the field is null"):

          given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it("should have returned a failure for the field with NoSuchElementException as the cause"):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

        describe("and the field is an empty string"):

          given source: Source = JsonCursor(fieldKey -> Json.fromString("")).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it(
            "should have returned a Binding Failure with a field error for the UUID field with an exception " +
              "that is not NoSuchElementException as the cause"
          ):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(
              BindingConfig.default.languageConfig.invalidUUIDMessage(fieldKey, "".quoted)
            )
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(false)

        describe("and the field is a whitespace string"):

          given source: Source = JsonCursor(fieldKey -> Json.fromString(whiteSpaceUUID)).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it(
            "should have returned a Binding Failure with a field error for the UUID field with an exception that " +
              "is not NoSuchElementException as the cause"
          ):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(
              BindingConfig.default.languageConfig.invalidUUIDMessage(fieldKey, whiteSpaceUUID.quoted)
            )
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(false)

        describe("and the field is not a valid UUID"):

          val fieldValue       = "someValue"
          given source: Source = JsonCursor(fieldKey -> Json.fromString(fieldValue)).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it(
            "should have returned a Binding Failure with a field error for the string field, and a cause that is not " +
              "NoSuchElementException"
          ):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
            val error = casted.fieldErrors.head
            error.messageParts should equal(
              BindingConfig.default.languageConfig.invalidUUIDMessage(fieldKey, fieldValue.quoted)
            )
            casted.cause.get.isInstanceOf[NoSuchElementException] should be(false)

        describe("and the value is a valid UUID"):

          val uuid       = UUID.randomUUID()
          val fieldValue = uuid.toString

          given source: Source = JsonCursor(fieldKey -> Json.fromString(fieldValue)).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it("should have bound the property including its spaces properly"):
            result should equal(BindingPass(uuid))
