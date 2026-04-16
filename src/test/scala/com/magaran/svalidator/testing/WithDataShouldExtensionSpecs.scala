package com.magaran.svalidator.testing

import com.magaran.svalidator.testing.exceptions.ValidationTestingException
import com.magaran.svalidator.testing.extensions.validation.withdata.shouldBeValid
import com.magaran.svalidator.validation.*
import testUtils.asKey
import testUtils.Observes

class WithDataShouldExtensionSpecs extends Observes:

  describe("when using the shouldBeValid view on a validation summary"):

    describe("and the summary is an instance of success with data"):

      describe("and the tested data matches"):
        val summary: ResultWithSuccessData[Int] = Valid(5)

        lazy val result =
          try Left(summary.shouldBeValid withData 5)
          catch case e: ValidationTestingException => Right(e)

        it("should not throw an exception when invoking the method upon the summary"):
          result.isLeft should be(true)

      describe("and the tested data does not match"):
        val summary: ResultWithSuccessData[Int] = Valid(5)

        lazy val result =
          try Left(summary.shouldBeValid withData 3)
          catch case e: ValidationTestingException => Right(e)

        it("should not throw an exception when invoking the method upon the summary"):
          result.isRight should be(true)

    describe("and the summary is an instance of failure"):
      val failures = nonEmptyList(
        ValidationFailure("aField".asKey, "aValue", ValidationMetadata.empty),
        ValidationFailure("anotherField".asKey, "anotherValue", ValidationMetadata.empty)
      )
      val summary: ResultWithSuccessData[Int] = Invalid(failures)

      lazy val result =
        try Left(summary.shouldBeValid)
        catch case e: ValidationTestingException => Right(e)

      it("should throw an exception when invoking the method upon the summary"):
        result.isRight should be(true)
