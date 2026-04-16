package com.magaran.svalidator.validation.binding

import com.magaran.svalidator.validation.ResultWithSuccessData

/** Base class for mapping binding validators with data, which are used to perform binding, then mapping of the bound
  * value and validation then of a given type and attach additional success data when both operations are successful.
  *
  * @tparam A Type of the instance being bound
  * @tparam B Type of the additional success data
  */
abstract class MappingBindingValidatorWithData[A, B]
    extends EssentialMappingBindingValidator[
      A,
      ResultWithSuccessData[B],
      BindingAndValidationResultWithSuccessData[A, B]
    ]
