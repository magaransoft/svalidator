package com.magaran.svalidator.binding.binders.typed

/** Performs binding of a [[scala.Float]] field */
final class FloatBinder extends NumericTypedBinder(_.toFloat, _.invalidFloatMessage)
