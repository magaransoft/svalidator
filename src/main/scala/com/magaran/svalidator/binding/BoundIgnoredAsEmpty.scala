package com.magaran.svalidator.binding

/** Trait to be mixed in by classes that even when bound successfully can be considered as empty when binding another
  * type that has then in the constructor.  If all values bound for a constructor are considered empty, then a no such
  * element exception is reported as the cause of the binding failure, allowing the OptionBinder to return None for those
  * cases.
  */
trait BoundIgnoredAsEmpty
