package com.magaran.svalidator.validation.binding
import scala.reflect.ClassTag

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.validation.ResultWithSuccessData

/** Base class for binding validators with data, which are used to perform binding and validation
  * in tandem of a given type and attach additional success data when both operations are successful.
  *
  * @tparam A Type of the instance being bound
  * @tparam B Type of the additional success data
  */
abstract class BindingValidatorWithData[A: { TypedBinder, ClassTag }, B]
    extends EssentialBindingValidator[A, ResultWithSuccessData[B], BindingAndValidationResultWithSuccessData[A, B]]
