package com.magaran.svalidator.testing.extensions.common

import com.magaran.svalidator.testing.exceptions.ValidationTestingException

/** Testing DSL continuation that asserts the success data of a valid result matches expected values.
  * Used after `shouldBeValid` on results that carry success data.
  */
class ShouldBeValidWithDataContinuationExtensions[A](data: A):

  infix def withData(expectedData: A): Unit =
    if data != expectedData then
      throw ValidationTestingException(
        s"\nExpected valid summary to have data $expectedData, but instead it had $data."
      )
