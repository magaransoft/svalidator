package com.magaran.svalidator.binding.binders.typed

import java.time.LocalTime

import com.magaran.svalidator.binding.config.BindingConfig

/** Performs binding of a [[java.time.LocalTime]] field */
final class LocalTimeBinder
    extends TemporalBinder[LocalTime](LocalTime.parse(_, summon[BindingConfig].timeFormatter), _.invalidTimeMessage)
