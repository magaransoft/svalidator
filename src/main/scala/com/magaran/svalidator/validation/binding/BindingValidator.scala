package com.magaran.svalidator.validation.binding

import scala.reflect.ClassTag

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.validation.ResultWithoutData

/** Base class for binding validators, which are used to perform binding and validation
  * in tandem of a given type without attaching additional data.
  *
  * @tparam A Type of the instance being bound
  */
abstract class BindingValidator[A: { TypedBinder, ClassTag }]
    extends EssentialBindingValidator[A, ResultWithoutData, BindingAndValidationResultWithoutData[A]]
