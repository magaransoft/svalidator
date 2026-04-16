package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.validation.ValidationFailure

/** Utility class that encapsulates a single validation criteria that can be applied to a given instance of a type
  *
  * @tparam A Type of instance being validated
  */
protected[internals] trait ValidationRule[-A]:

  /** Applies this validation rule to the given instance
    *
    * @param instance The instance to validate
    * @return A list of validation failures, or an empty list if the application of this rule was successful
    */
  def apply(instance: A): Vector[ValidationFailure]
