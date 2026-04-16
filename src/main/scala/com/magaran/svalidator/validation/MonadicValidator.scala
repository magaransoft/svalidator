package com.magaran.svalidator.validation

/** Functional style variant of the [[com.magaran.svalidator.validation.Validator Validator]] trait
  *
  * @tparam Instance Type of objects to be validated
  */
trait MonadicValidator[Instance, InputData, Context, SuccessData] {

  /** Returns a [[com.magaran.svalidator.validation.ValidationResult]] with error information
    * from validating the instance
    *
    * @param instance Instance to validate
    */
  def validate(instance: Instance, inputData: InputData)(using Context): Either[Invalid, SuccessData]

}
