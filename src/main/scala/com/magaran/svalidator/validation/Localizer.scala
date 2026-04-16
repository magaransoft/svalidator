package com.magaran.svalidator.validation

/** Provides functionality for localizing error message keys in validation classes. */
trait Localizer:

  /** Returns the localized message for the given message key
    *
    * @param messageKey Message key to localize
    */
  def localize(messageKey: String): String

object Localizer:

  /** A [[Localizer]] that does not localize the message keys, and returns them as is. */
  object NoLocalization extends Localizer:
    override def localize(messageKey: String): String = messageKey
