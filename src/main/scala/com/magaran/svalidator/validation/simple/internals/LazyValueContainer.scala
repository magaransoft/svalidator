package com.magaran.svalidator.validation.simple.internals

/** Utility class that encapsulates a lazy value.
  *
  * It is used to allow chained dependent validation streams to only evaluate their target properties if the upstream
  * validation is valid.
  *
  * @param value Lazy value to be stored for later reference
  * @tparam A The type of the lazy value
  */
protected[internals] final class LazyValueContainer[A](value: => A):

  def extractValue: A = value
