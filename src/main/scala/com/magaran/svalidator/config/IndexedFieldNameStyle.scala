package com.magaran.svalidator.config

/** Configuration value determining how to represent indexed field access
  *
  * @see [[com.magaran.svalidator.config.IndexedFieldNameStyle.MixedNotation MixedNotation]]
  */
enum IndexedFieldNameStyle:

  /** Style using dot notation for sub-fields and brackets for indexed access (e.g.: myRootField.myNestedField[3]) */
  case MixedNotation

  protected[svalidator] final def getIndexedInfoForIndex(index: Int): String =
    this match
      case MixedNotation => s"[$index]"

  private def formatFieldNameTokens(fieldNameTokens: Seq[String]): String =
    this match
      case MixedNotation =>
        fieldNameTokens.zipWithIndex.collect {
          case (token, index) if index == 0           => token
          case (token, _) if token.exists(!_.isDigit) => s".$token"
          case (token, _)                             => s"[$token]"
        }.mkString

  /** Normalizes a field key string into the canonical format for this style, handling mixed bracket and dot notation. */
  def normalizeKey(key: String): String =
    val dotNotationKey = key.filter(_ != ']').replace('[', '.')
    val tokens         = dotNotationKey.split("\\.").map(_.trim).filterNot(_.isEmpty).toList
    formatFieldNameTokens(tokens)
