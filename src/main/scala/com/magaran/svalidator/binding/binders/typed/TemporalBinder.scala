package com.magaran.svalidator.binding.binders.typed

import java.time.format.DateTimeParseException
import java.time.temporal.Temporal

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.NestedFieldKey

/** Base class for binders that bind a [[java.time.temporal.Temporal]] type
  *
  * [[java.time.LocalDate]] and [[java.time.LocalTime]] have implementations provided by default.
  *
  * @param convertFromString Function that parses the string value into the temporal type
  * @param invalidMessageFunction A function from [[com.magaran.svalidator.binding.config.BindingLanguageConfig]] that gives the invalid message for the type
  * @tparam A Type of the resulting bound value
  */
protected[typed] abstract class TemporalBinder[A <: Temporal: TypeShow](
  convertFromString: BindingConfig ?=> String => A,
  invalidMessageFunction: BindingLanguageConfig => (NestedFieldKey, String) => MessageParts
) extends EssentialNonRootBinder[A, String](convertFromString, convertFromString, invalidMessageFunction):

  override def exceptionHandler(
    fieldKey: NestedFieldKey,
    fieldValue: String
  )(using Source, BindingConfig, BindingContext): PartialFunction[Throwable, BindingResult[A]] =
    case ex: DateTimeParseException =>
      BindingFailure(fieldKey, invalidMessageFunction(summon[BindingLanguageConfig])(fieldKey, fieldValue), Some(ex))
