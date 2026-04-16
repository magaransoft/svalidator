package com.magaran.svalidator.binding.binders.typed

/** Performs binding of an [[scala.Int]] field */
final class IntBinder extends NumericTypedBinder(_.toInt, _.invalidIntegerMessage)
