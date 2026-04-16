package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.Invalid

/** Alias for a [[com.magaran.svalidator.validation.functional.FunctionalValidator FunctionalValidator]] that
  * has no input data and does not return any success data.
  *
  * @tparam Instance    Objects to be validated by this class
  * @tparam Context     Implicit context used to validate the instance (e.g. request context/ session context)
  */
abstract class ContextualValidator[Instance, Context] extends FunctionalValidator[Instance, Unit, Context, Unit] {

  def validate(instance: Instance)(using Context): Either[Invalid, Unit]

  final override def validate(instance: Instance, inputData: Unit)(using Context): Either[Invalid, Unit] = {
    validate(instance = instance)
  }
}
