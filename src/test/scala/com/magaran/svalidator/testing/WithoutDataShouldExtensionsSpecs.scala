package com.magaran.svalidator.testing

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.testing.extensions.validation.withoutdata.shouldBeValid
import com.magaran.svalidator.testing.extensions.validation.withoutdata.shouldHaveValidationErrorFor
import com.magaran.svalidator.testing.extensions.validation.withoutdata.shouldNotHaveValidationErrorFor
import com.magaran.svalidator.validation.*
import testUtils.asKey
import testUtils.Observes

class WithoutDataShouldExtensionsSpecs extends Observes:

  describe("when using the shouldBeValid view on a validation summary"):

    describe("and the summary is the instance of success without data"):

      val summary: ResultWithoutData = Valid

      lazy val result =
        try Left(summary.shouldBeValid())
        catch case e: ValidationTestingException => Right(e)

      it("should not throw an exception when invoking the method upon the summary"):
        result.isLeft should be(true)

    describe("and the summary is an instance of failure"):
      val failures = nonEmptyList(
        ValidationFailure("aField".asKey, "aValue", ValidationMetadata.empty),
        ValidationFailure("anotherField".asKey, "anotherValue", ValidationMetadata.empty)
      )
      val summary: ResultWithoutData = Invalid(failures)

      lazy val result =
        try Left(summary.shouldBeValid())
        catch case e: ValidationTestingException => Right(e)

      it("should throw an exception when invoking the method upon the summary"):
        result.isRight should be(true)

  describe("when using the shouldHaveValidationErrorFor view on a validation summary"):

    describe("and there's no error for the specified field"):

      val failures = nonEmptyList(
        ValidationFailure("aField".asKey, MessageParts("aMessage"), ValidationMetadata.empty),
        ValidationFailure("anotherField".asKey, MessageParts("anotherMessage"), ValidationMetadata.empty)
      )
      val summary: ResultWithoutData = Invalid(failures)

      lazy val result =
        try Left(summary shouldHaveValidationErrorFor "aDifferentField")
        catch case e: ValidationTestingException => Right(e)

      it("should have thrown a validation testing exception"):
        result.isRight should be(true)

    describe("and there's an error for the specified field"):

      val failures = nonEmptyList(
        ValidationFailure("aField".asKey, MessageParts("aMessage"), ValidationMetadata.empty),
        ValidationFailure("aDifferentField".asKey, MessageParts("anotherMessage"), ValidationMetadata.empty)
      )
      val summary: ResultWithoutData = Invalid(failures)

      lazy val unitResult =
        try Left(summary shouldHaveValidationErrorFor "aDifferentField")
        catch case e: ValidationTestingException => Right(e)

      it("should not have thrown a validation testing exception"):
        unitResult.isLeft should be(true)

  describe("when using the shouldNotHaveValidationErrorFor view on a validation summary"):

    describe("and there's no error for specified field"):

      val failures = nonEmptyList(
        ValidationFailure("aField".asKey, MessageParts("aMessage"), ValidationMetadata.empty),
        ValidationFailure("anotherField".asKey, MessageParts("anotherMessage"), ValidationMetadata.empty)
      )
      val summary: ResultWithoutData = Invalid(failures)

      lazy val unitResult =
        try Left(summary shouldNotHaveValidationErrorFor "aDifferentField")
        catch case e: ValidationTestingException => Right(e)

      it("should not have thrown a validation testing exception"):
        unitResult.isLeft should be(true)

    describe("and there's an error for the specified field"):

      val failures = nonEmptyList(
        ValidationFailure("aField".asKey, MessageParts("aMessage"), ValidationMetadata.empty),
        ValidationFailure("aDifferentField".asKey, MessageParts("anotherMessage"), ValidationMetadata.empty)
      )
      val summary: ResultWithoutData = Invalid(failures)

      lazy val result =
        try Left(summary shouldNotHaveValidationErrorFor "aDifferentField")
        catch case e: ValidationTestingException => Right(e)

      it("should have thrown a validation testing exception"):
        result.isRight should be(true)
