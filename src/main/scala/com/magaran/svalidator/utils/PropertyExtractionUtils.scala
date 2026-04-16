package com.magaran.svalidator.utils

import scala.annotation.tailrec
import scala.quoted.*

/** Compile-time utilities for extracting property names from lambda expressions in macro contexts.
  *
  * Supports patterns like `_.myProperty`, `x => x.myProperty`, and nested selections like `_.parent.child`.
  * Used internally by rule builders to derive field names from property accessors without requiring
  * explicit string field names.
  */
object PropertyExtractionUtils {

  @tailrec
  protected[svalidator] def unwrapInlines[Q <: Quotes](using q: Q)(term: q.reflect.Term): q.reflect.Term = {
    import quotes.reflect.*
    term match
      case Inlined(_, _, term) => unwrapInlines(term)
      case _                   => term
  }

  protected[svalidator] def extractPropertyNameFromBlockAST(
    using q: Quotes
  )(propertyExpressionTerm: q.reflect.Term, alternativeMethodForArbitraryExpressions: String): String = {
    import quotes.reflect.*
    propertyExpressionTerm match
      // Matches pattern: For { _.myProperty }
      case Block(_, Block(List(DefDef(_, _, _, Some(select @ Select(_, _)))), _)) =>
        recursivelyExtractSelectedProperty(select, alternativeMethodForArbitraryExpressions)
      // Matches pattern: For { x => x.myProperty }
      case Block(_, Block(List(DefDef(_, _, _, Some(Block(_, select @ Select(_, _))))), _)) =>
        recursivelyExtractSelectedProperty(select, alternativeMethodForArbitraryExpressions)
      // Matches 2 patterns: For(_.myProperty)     For(x => x.myProperty)
      case Block(List(DefDef(_, _, _, Some(select @ Select(_, _)))), _) =>
        recursivelyExtractSelectedProperty(select, alternativeMethodForArbitraryExpressions)
      case _ => reportErrorForExtractionFailure(alternativeMethodForArbitraryExpressions)
  }

  private def recursivelyExtractSelectedProperty(
    using q: Quotes
  )(term: q.reflect.Term, alternativeMethodForArbitraryExpressions: String): String = {
    import quotes.reflect.*
    term match
      case Select(Ident(_), fieldName) => fieldName
      case Select(innerTerm, fieldName) =>
        val selectedLeadingTermField =
          recursivelyExtractSelectedProperty(innerTerm, alternativeMethodForArbitraryExpressions)
        s"$selectedLeadingTermField.$fieldName"
      case _ => reportErrorForExtractionFailure(alternativeMethodForArbitraryExpressions)
  }

  private def reportErrorForExtractionFailure(alternativeMethodForArbitraryExpressions: String)(using Quotes) = {
    import quotes.reflect.report
    report.errorAndAbort(
      s"""
         |Could not extract property name from expression
         |Please a property extractor like 'x => x.myProperty' or '_.myProperty'
         |Nested properties are allowed, but you can not call methods in any part of the chain.
         |Consider using $alternativeMethodForArbitraryExpressions instead if you'd like to use arbitrary expressions with an explicit
         |field name.
         |""".stripMargin.trim
    )
  }
}
