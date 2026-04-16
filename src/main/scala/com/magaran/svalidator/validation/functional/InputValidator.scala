package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.Invalid

/** Alias for a [[com.magaran.svalidator.validation.functional.FunctionalValidator FunctionalValidator]] that
  * does not return any success data and has no implicit context.
  *
  * @tparam Instance    Objects to be validated by this class
  * @tparam InputData   Additional data used to validate the instance (e.g. database results)
  */
abstract class InputValidator[Instance, InputData] extends FunctionalValidator[Instance, InputData, Unit, Unit] {

  def validate(instance: Instance, input: InputData): Either[Invalid, Unit]

  final override def validate(instance: Instance, inputData: InputData)(
    using context: Unit = ()
  ): Either[Invalid, Unit] = {
    validate(instance = instance, input = inputData)
  }

}
