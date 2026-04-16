package com.magaran.svalidator.validation

/** Base trait for validation of objects of a given type
  *
  * @tparam A Type of objects to be validated
  */
trait Validator[-A, +B <: ValidationResult]:

  /** Returns a [[com.magaran.svalidator.validation.ValidationResult]] with error information
    * from validating the instance
    *
    * @param instance Instance to validate
    */
  def validate(using instance: A): B
