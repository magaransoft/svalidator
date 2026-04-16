package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/** Binds lists of a given type, provided that a binder for the type parameter is provided.
  *
  * @param underlyingBinder The binder for the type of elements to be used in the bound list
  */
final class VectorBinder[A](using underlyingBinder: TypedBinder[A], typeShow: TypeShow[Vector[A]])
    extends TypedBinder[Vector[A]]:

  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[Vector[A]] =
    SequenceBinderUtils.bindSequenceAndConvert(underlyingBinder, fieldKey, source, _.toVector)
