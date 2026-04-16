package com.magaran.svalidator.binding.binders.typed

import java.util.UUID

import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingFailure
import com.magaran.svalidator.binding.BindingResult
import com.magaran.svalidator.binding.Source
import com.magaran.svalidator.NestedFieldKey

/** Binder that parses a string value into a [[java.util.UUID]], producing a binding failure
  * with a localized invalid-UUID message if the format is incorrect.
  */
class UUIDBinder extends EssentialNonRootBinder[UUID, String](UUID.fromString, UUID.fromString, _.invalidUUIDMessage):
  override def exceptionHandler(
    fieldKey: NestedFieldKey,
    fieldValue: String
  )(using Source, BindingConfig, BindingContext): PartialFunction[Throwable, BindingResult[UUID]] =
    case ex: IllegalArgumentException =>
      BindingFailure(fieldKey, summon[BindingLanguageConfig].invalidUUIDMessage(fieldKey, fieldValue), Some(ex))
