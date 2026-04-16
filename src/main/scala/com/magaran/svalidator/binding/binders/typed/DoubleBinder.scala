package com.magaran.svalidator.binding.binders.typed

/** Performs binding of a [[scala.Double]] field */
final class DoubleBinder extends NumericTypedBinder(_.toDouble, _.invalidDoubleMessage)
