package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class BooleanBinderSpecs extends Observes:

  private val fieldName                             = "someBooleanFieldName"
  private val fieldKey                              = NestedFieldKey(fieldName)
  private given config: BindingConfig               = BindingConfig.default
  private given metadata: BindingContext            = BindingContext.empty
  private val languageConfig: BindingLanguageConfig = BindingConfig.default.languageConfig
  private val sut: TypedBinder[Boolean]             = BooleanBinder()

  describe("when binding a Boolean value"):

    describe("and the values map method of binding is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherBoolean") -> List("true"))
        val result           = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          result should equal(BindingPass(false))

      describe("and the argument is present in the values map with a false value"):

        given source: Source = ValuesMap(fieldKey -> List("false"))
        val result           = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          result should equal(BindingPass(false))

      describe("and the argument is present in the values map with a true value"):

        given source: Source = ValuesMap(fieldKey -> List("true"))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          result should equal(BindingPass(true))

      describe("and the argument is present in the values map with a value that is not a Boolean"):

        val invalidFieldValue = "18"

        given source: Source = ValuesMap(fieldKey -> List(invalidFieldValue))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an invalid boolean error for the field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.invalidBooleanMessage(fieldKey, invalidFieldValue.quoted))

    describe("and the json source method of binding is used"):

      describe("and the argument is not present in the source"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherBoolean") -> Json.fromBoolean(true)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should return a Binding Failure with no such element exception as the cause"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the source with a false value"):
        given source: Source = JsonCursor(fieldKey -> Json.fromBoolean(false)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          result should equal(BindingPass(false))

      describe("and the argument is present in the source with a true value"):

        given source: Source = JsonCursor(fieldKey -> Json.fromBoolean(true)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          result should equal(BindingPass(true))

      describe("and the argument is present in the source with a value of null"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(languageConfig.invalidBooleanMessage(fieldKey, "null"))

      describe("and the argument is present in the source with a value that is not a Boolean"):

        val invalidFieldValue = 18
        given source: Source  = JsonCursor(fieldKey -> Json.fromInt(invalidFieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Pass with the value set to false"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            languageConfig.invalidBooleanMessage(fieldKey, invalidFieldValue.toString.quoted)
          )
