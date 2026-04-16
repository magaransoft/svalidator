package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingResult
import com.magaran.svalidator.binding.Source
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/** Binds lists of a given type, provided that a binder for the type parameter is provided. */
final class SetBinder[A](using TypedBinder[A], TypeShow[Set[A]]) extends TypedBinder[Set[A]]:

  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[Set[A]] =
    SequenceBinderUtils.bindSequenceAndConvert(summon[TypedBinder[A]], fieldKey, source, _.toSet)
