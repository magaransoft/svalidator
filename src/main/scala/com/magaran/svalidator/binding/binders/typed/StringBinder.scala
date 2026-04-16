package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.config.StringBindingStyle
import com.magaran.svalidator.NestedFieldKey

/** Performs binding of a [[java.lang.String]] field.
  *
  * Only strings that are non-whitespace and non-empty will be successfully bound.
  * Depending on the string binding style, spaces may be preserved or trimmed after binding.
  */
final class StringBinder
    extends EssentialNonRootBinder[String, String](
      StringBinder.inboundStringHandler,
      StringBinder.inboundStringHandler,
      _.invalidNonEmptyStringMessage
    ):

  protected[typed] override def beforeConvertFromStringHook: String => String = identity

  protected[typed] override def postExtractIsValidFilter: String => Boolean = !_.isBlank

  override def exceptionHandler(fieldKey: NestedFieldKey, fieldValue: String)(
    using source: Source,
    config: BindingConfig,
    context: BindingContext
  ): PartialFunction[Throwable, BindingResult[String]] =
    case _ if source.isInstanceOf[JsonCursor] && source.asInstanceOf[JsonCursor].focus.exists(_.isNull) =>
      BindingFailure(fieldKey, config.languageConfig.invalidNonEmptyStringMessage(fieldKey, fieldValue), None)
    case ex: NoSuchElementException if fieldValue.nonEmpty =>
      BindingFailure(fieldKey, config.languageConfig.invalidNonEmptyStringMessage(fieldKey, fieldValue), Some(ex))

end StringBinder

object StringBinder:

  private def inboundStringHandler: BindingConfig ?=> String => String = x =>
    summon[BindingConfig].stringBindingStyle match
      case StringBindingStyle.InvalidateFullWhitespacePreserveWhitespaces => x
      case StringBindingStyle.TrimWhitespaceInvalidateEmpty               => x.trim
