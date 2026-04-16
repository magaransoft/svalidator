package com.magaran.svalidator.validation.simple

import com.magaran.svalidator.validation.*
import com.magaran.svalidator.validation.simple.internals.EssentialValidator

/** Base class to provide fluent validation syntax for validation that returns additional success data
  *
  * @tparam A Type of objects to be validated by this class
  * @tparam B Type of data included with successful summaries of this class
  */
abstract class SimpleValidatorWithData[A, B] extends EssentialValidator[A, ResultWithSuccessData[B]]
