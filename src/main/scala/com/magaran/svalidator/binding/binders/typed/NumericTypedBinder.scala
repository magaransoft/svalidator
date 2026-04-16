package com.magaran.svalidator.binding.binders.typed

import scala.annotation.nowarn

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.NestedFieldKey
import io.circe.Decoder

/** Base class for binders that bind a numeric type
  *
  * @param convertFromString Conversion function from string to the numeric type
  * @param invalidMessageFunction A function from [[com.magaran.svalidator.binding.config.BindingLanguageConfig]] that gives the invalid message for the type
  * @tparam A Type of the resulting bound value
  */
@nowarn("msg=unused implicit parameter")
//We intentionally require a Numeric context bound to restrict types for this binder, even if it isn't used
protected[typed] abstract class NumericTypedBinder[A: { Numeric, Decoder, TypeShow }](
  convertFromString: String => A,
  invalidMessageFunction: BindingLanguageConfig => (NestedFieldKey, String) => MessageParts
) extends PrimitiveBinder(convertFromString, invalidMessageFunction):

  override def exceptionHandler(
    fieldKey: NestedFieldKey,
    fieldValue: String
  )(using Source, BindingConfig, BindingContext): PartialFunction[Throwable, BindingResult[A]] =
    case ex: NumberFormatException =>
      BindingFailure(fieldKey, invalidMessageFunction(summon[BindingLanguageConfig])(fieldKey, fieldValue), Some(ex))
