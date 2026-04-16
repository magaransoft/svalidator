package com.magaran.svalidator.binding.binders.special

import scala.reflect.Selectable.reflectiveSelectable

import com.magaran.svalidator.binding.binders.BinderUtils
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingFailure
import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.binding.BindingResult
import com.magaran.svalidator.binding.Source
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/** Binder for enum sum types that have an id field and are therefore stable for serialization/storage. */
class StableEnumBinder[A](enumValues: Array[A])(using TypeShow[A]) extends TypedBinder[A]:

  // noinspection LanguageFeature
  private val valuesMap: Map[Int, A] = enumValues
    .map: value =>
      value.asInstanceOf[{ def id: Int }].id -> value
    .toMap

  def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[A] =
    BinderUtils.nonRootBinding(source, fieldKey): nestedFieldKey =>
      val intBoundId = summon[TypedBinder[Int]].bind(source, fieldKey)
      intBoundId match
        case BindingPass(id) =>
          valuesMap.get(id) match
            case Some(value) => BindingPass(value)(using source)
            case None =>
              BindingFailure(
                nestedFieldKey,
                summon[BindingLanguageConfig]
                  .invalidEnumerationMessage(summon[TypeShow[A]].targetTypeName)(nestedFieldKey, id.toString),
                None
              )(using source)
        case f: BindingFailure => f
