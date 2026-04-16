package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.quoted
import testUtils.Observes

class BigDecimalBinderSpecs extends Observes:

  private val fieldName                  = "someDecimalFieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)
  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty

  describe("when binding a BigDecimal value"):

    val sut: TypedBinder[BigDecimal] = BigDecimalBinder()

    describe("and the string values map version of the method is used"):

      describe("and the argument is not present in the values map"):

        given source: Source = ValuesMap(NestedFieldKey("someOtherDouble") -> List("315.00"))

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned a Binding Failure with an error for the decimal field with a NoSuchElementException as the cause"
        ):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.fieldKey should equal(fieldKey)
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the values map but it is not a valid decimal"):

        val invalidFieldValue = "aStringThatCanNotBeParsedAsDecimal"
        given source: Source  = ValuesMap(fieldKey -> List(invalidFieldValue))

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the decimal field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidDecimalMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe("and the argument is present in the values map and is a valid decimal"):

        given source: Source = ValuesMap(fieldKey -> List("170.5000"))
        val result           = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(BigDecimal("170.5000")))

    describe("and the json version of the source is used"):

      describe("and the argument is not present in the values map"):

        given source: Source =
          JsonCursor(NestedFieldKey("someOtherDouble") -> Json.fromString("315.00")).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the decimal field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.fieldKey should equal(fieldKey)
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present but has a value of null"):

        given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the decimal field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.fieldKey should equal(fieldKey)
          error.messageParts should equal(BindingConfig.default.languageConfig.noValueProvidedMessage(fieldKey))
          casted.cause.get.isInstanceOf[NoSuchElementException] should be(true)

      describe("and the argument is present in the json but it is not a valid decimal"):

        val invalidFieldValue = "aStringThatCanNotBeParsedAsDecimal"
        given source: Source  = JsonCursor(fieldKey -> Json.fromString(invalidFieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have returned a Binding Failure with an error for the decimal field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors.filter(_.fieldKey == fieldKey) should have size 1
          val error = casted.fieldErrors.head
          error.fieldKey should equal(fieldKey)
          error.messageParts should equal(
            BindingConfig.default.languageConfig.invalidDecimalMessage(fieldKey, invalidFieldValue.quoted)
          )

      describe("and the argument is present in the values map and is a valid decimal"):

        val targetValue      = "170.5000"
        given source: Source = JsonCursor(fieldKey -> Json.fromString(targetValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should have bound the valueGetter  properly"):
          result should equal(BindingPass(BigDecimal(targetValue)))
