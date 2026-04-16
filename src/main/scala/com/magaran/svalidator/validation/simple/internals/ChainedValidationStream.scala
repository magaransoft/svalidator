package com.magaran.svalidator.validation.simple.internals

/** Represents a sequence of rules to be applied to an instance, and collection of upstream dependency rules that must
  * be valid for the rules in this stream to be applied.
  *
  * @tparam A Type of instance being validated
  */
protected[internals] final class ChainedValidationStream[-A](
  val mainStream: Vector[LazyList[ValidationRule[A]]],
  val dependsOnUpstream: Option[RuleStreamCollection[A]]
)
