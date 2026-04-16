package com.magaran.svalidator.validation.simple.internals

/** Contains chains of validation streams to apply to an instance.  Each chain is a sequence of rules to be applied to
  * a given property, which may have been mapped or switched field names along the way
  *
  * @tparam A Type object being validated
  */
protected[internals] final class RuleStreamCollection[-A](val chains: Vector[ChainedValidationStream[A]]) {}

protected[internals] object RuleStreamCollection:

  /** Returns an empty RuleStreamCollection */
  val Empty: RuleStreamCollection[Any] = RuleStreamCollection(Vector.empty)
