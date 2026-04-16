package com.magaran.svalidator.binding

/** Trait to be mixed in by classes that when bound successfully can conditionally be considered as empty.
  *
  * If all values bound for a constructor are considered empty, then a no such
  * element exception is reported as the cause of the binding failure, allowing the OptionBinder to return None for those
  * cases.  The value returned by the isEmpty method is used to determine if the value should be considered empty.
  */
trait CanBeBoundIgnoredAsEmpty:

  def isEmpty: Boolean
