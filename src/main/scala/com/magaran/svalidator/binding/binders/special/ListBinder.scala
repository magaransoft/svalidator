package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/** Binds lists of a given type, provided that a binder for the type parameter is provided. */
final class ListBinder[A](using TypedBinder[A], TypeShow[List[A]]) extends TypedBinder[List[A]]:

  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[List[A]] =
    SequenceBinderUtils.bindSequenceAndConvert(summon[TypedBinder[A]], fieldKey, source, _.toList)
