package com.magaran.svalidator.config

/** Specifies the style how to manage missing boolean value bindings when the source is a ValuesMap */
enum BooleanValuesMapBindingStyle:
  /** Boolean binding style that binds missing values as false */
  case BindMissingAsFalse

  /** Boolean binding style that generates a BindingFailure with NoSuchElementException as cause a value is missing */
  case BindingFailureOnMissingValue
