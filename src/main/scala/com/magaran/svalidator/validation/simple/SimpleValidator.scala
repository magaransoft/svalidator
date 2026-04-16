package com.magaran.svalidator.validation.simple

import com.magaran.svalidator.validation.simple.internals.EssentialValidator
import com.magaran.svalidator.validation.ResultWithoutData

/** Base class to provide fluent validation syntax, if no success data is required to be returned from the validation
  *
  * @tparam A Type of objects to be validated by this class
  */
abstract class SimpleValidator[A] extends EssentialValidator[A, ResultWithoutData]
