package com.magaran.svalidator.validation.functional

import com.magaran.svalidator.validation.Invalid

/** Alias for a [[com.magaran.svalidator.validation.functional.FunctionalValidator FunctionalValidator]] that
  * does not return any success data, has no implicit context and does not require any input data.
  *
  * @tparam Instance    Objects to be validated by this class
  */
abstract class InstanceValidator[Instance] extends FunctionalValidator[Instance, Unit, Unit, Unit] {

  def validate(instance: Instance): Either[Invalid, Unit]

  final override def validate(instance: Instance, inputData: Unit)(using Unit): Either[Invalid, Unit] = {
    validate(instance)
  }
}
