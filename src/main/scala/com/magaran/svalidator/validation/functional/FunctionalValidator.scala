package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.MonadicValidator

/** Base class to provide fluent validation syntax in the functional style of validation.
  *
  * Unlike [[com.magaran.svalidator.validation.simple.SimpleValidator SimpleValidator]], the same variant could be used
  * for validations both with and without success data.  In the case you do not need input data, context, or success data,
  * you can use Unit as the type for those parameters, or use one of the provided classes in this package as aliases
  *
  * @see [[com.magaran.svalidator.validation.functional.InstanceValidator InstanceValidator]]
  * @see [[com.magaran.svalidator.validation.functional.InputValidator InputValidator]]
  * @see [[com.magaran.svalidator.validation.functional.ContextualValidator ContextualValidator]]
  *
  * @tparam Instance    Objects to be validated by this class
  * @tparam InputData   Additional data used to validate the instance (e.g. database results)
  * @tparam Context     Implicit context used to validate the instance (e.g. request context/ session context)
  * @tparam SuccessData Data returned on successful validation of the instance
  */
abstract class FunctionalValidator[Instance, InputData, Context, SuccessData]
    extends MonadicValidator[Instance, InputData, Context, SuccessData]
    with SharedFunctionalRuleBuilders[Instance]
