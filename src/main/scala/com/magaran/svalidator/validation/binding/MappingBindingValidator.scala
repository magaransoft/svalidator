package com.magaran.svalidator.validation.binding

import com.magaran.svalidator.validation.ResultWithoutData

/** Base class for mapping binding validators, which are used to perform binding, then mapping of the value,
  * and then and validation of a given type without attaching additional data.
  *
  * @tparam A Type of the instance being bound
  */
abstract class MappingBindingValidator[A]
    extends EssentialMappingBindingValidator[A, ResultWithoutData, BindingAndValidationResultWithoutData[A]]
