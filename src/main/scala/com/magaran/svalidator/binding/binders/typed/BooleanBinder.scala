package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.config.BooleanValuesMapBindingStyle
import com.magaran.svalidator.NestedFieldKey

/** Performs binding of a [[scala.Boolean]] field
  *
  *  Boolean binding is special compared to other types because lack of a value will not result
  *  in a [[BindingFailure]], instead, a [[BindingPass]] with a bound value of <code>false</code>
  *  will be returned.  This is done to match checkbox behavior of traditional HTML browsers where no
  *  value is sent when the checkbox is unchecked.
  *
  *  Additionally, if the binding is from a [[JsonCursor]] source, <code>null</code> values will cause a [[BindingFailure]]
  *  as null values received in place of booleans in a json are most likely a sign of a programming issue
  *  rather than intended behavior.
  */
final class BooleanBinder extends PrimitiveBinder(_.toBoolean, _.invalidBooleanMessage):

  override def exceptionHandler(fieldKey: NestedFieldKey, fieldValue: String)(
    using source: Source,
    config: BindingConfig,
    context: BindingContext
  ): PartialFunction[Throwable, BindingResult[Boolean]] =
    case ex: NoSuchElementException =>
      import BooleanValuesMapBindingStyle._
      source match
        case _: ValuesMap =>
          config.booleanValuesMapBindingStyle match {
            case BindMissingAsFalse => BindingPass(false)
            case BindingFailureOnMissingValue =>
              BindingFailure(fieldKey, config.languageConfig.noValueProvidedMessage(fieldKey), Some(ex))
          }

        case cursor: JsonCursor =>
          if cursor.focus.exists(_.isNull) then
            BindingFailure(fieldKey, config.languageConfig.invalidBooleanMessage(fieldKey, "null"), None)
          else BindingFailure(fieldKey, config.languageConfig.noValueProvidedMessage(fieldKey), Some(ex))

    case ex: IllegalArgumentException =>
      BindingFailure(fieldKey, config.languageConfig.invalidBooleanMessage(fieldKey, fieldValue), Some(ex))
