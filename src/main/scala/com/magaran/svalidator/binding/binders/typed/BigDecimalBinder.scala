package com.magaran.svalidator.binding.binders.typed

/** Performs binding of a [[scala.math.BigDecimal]] field */
final class BigDecimalBinder extends NumericTypedBinder(BigDecimal.apply, _.invalidDecimalMessage)
