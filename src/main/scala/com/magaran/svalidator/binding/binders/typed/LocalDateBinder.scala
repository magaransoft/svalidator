package com.magaran.svalidator.binding.binders.typed

import java.time.LocalDate

import com.magaran.svalidator.binding.config.BindingConfig

/** Performs binding of a [[java.time.LocalDate]] field */
final class LocalDateBinder
    extends TemporalBinder[LocalDate](LocalDate.parse(_, summon[BindingConfig].dateFormatter), _.invalidDateMessage)
