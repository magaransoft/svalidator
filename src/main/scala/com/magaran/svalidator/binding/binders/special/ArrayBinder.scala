package com.magaran.svalidator.binding.binders.special

import scala.reflect.ClassTag

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/** Binds lists of a given type, provided that a binder for the type parameter is provided.
  *
  * @param underlyingBinder The binder for the type of elements to be used in the bound list
  */
final class ArrayBinder[A](using underlyingBinder: TypedBinder[A], classTag: ClassTag[A], typeShow: TypeShow[Array[A]])
    extends TypedBinder[Array[A]]:

  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[Array[A]] =
    SequenceBinderUtils.bindSequenceAndConvert(
      underlyingBinder,
      fieldKey,
      source,
      x => Array.newBuilder(using classTag).addAll(x.asInstanceOf[IterableOnce[A]]).result(),
    )

  override def toString: String =
    super.toString.replace("Binder", s"Binder[${classTag.runtimeClass.getSimpleName}]")
