package com.magaran.svalidator.binding.config

import java.time.format.DateTimeFormatter

import com.magaran.svalidator.config.BooleanValuesMapBindingStyle
import com.magaran.svalidator.config.IndexedFieldNameStyle
import com.magaran.svalidator.config.StringBindingStyle

/** Specifies configuration values passed to all binders */
trait BindingConfig:

  /** Returns the formatter to use for binding [[java.time.LocalDate]]s */
  def dateFormatter: DateTimeFormatter

  /** Returns the formatter to use for binding [[java.time.LocalTime]]s */
  def timeFormatter: DateTimeFormatter

  /** Returns the indexing style used when parsing/creating indexed fields during binding/validation respectively */
  final val indexedFieldNameStyle: IndexedFieldNameStyle = IndexedFieldNameStyle.MixedNotation

  /** Returns the string binding style to use for binding strings */
  def stringBindingStyle: StringBindingStyle

  /** Returns the configuration of error messages for binders */
  def languageConfig: BindingLanguageConfig

  /** Returns the binding style to use for binding booleans from values maps */
  def booleanValuesMapBindingStyle: BooleanValuesMapBindingStyle

object BindingConfig:

  /** Returns the default implementation for binding configuration */
  final val default: BindingConfig = new BindingConfig:
    val dateFormatter: DateTimeFormatter                           = DateTimeFormatter.ISO_LOCAL_DATE
    val timeFormatter: DateTimeFormatter                           = DateTimeFormatter.ISO_LOCAL_TIME
    val stringBindingStyle: StringBindingStyle                     = StringBindingStyle.InvalidateFullWhitespacePreserveWhitespaces
    val booleanValuesMapBindingStyle: BooleanValuesMapBindingStyle = BooleanValuesMapBindingStyle.BindMissingAsFalse
    val languageConfig: BindingLanguageConfig                      = BindingLanguageConfig.defaultConfig
