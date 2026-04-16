package com.magaran.svalidator.binding.binders.typed

/** Performs binding of a [[scala.Long]] field */
final class LongBinder extends NumericTypedBinder[Long](_.toLong, _.invalidLongMessage)
