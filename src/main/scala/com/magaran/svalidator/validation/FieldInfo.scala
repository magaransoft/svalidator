package com.magaran.svalidator.validation

import com.magaran.svalidator.utils.PropertyExtractionUtils
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

/** Pairs a property extractor function with its corresponding [[FieldKey]], derived at compile time.
  *
  * @tparam A the type of the object being validated
  * @tparam B the type of the extracted property
  * @param extractor function that extracts the property value from an instance of `A`
  * @param fieldKey the key identifying the field, derived from the property accessor's AST
  */
case class FieldInfo[A, B](extractor: A => B, fieldKey: FieldKey)

/** Factory methods for creating [[FieldInfo]] instances via compile-time macro extraction of property names. */
object FieldInfo {

  inline protected[svalidator] def apply[A, B](
    inline propertyExtractor: A => B,
    alternativeMethodForArbitraryExpressions: String
  ): FieldInfo[A, B] = ${
    fieldInfoMacroImpl[A, B]('propertyExtractor, 'alternativeMethodForArbitraryExpressions)
  }

  inline protected[validation] def applySeq[A, B](
    inline propertyExtractors: Seq[A => B],
    alternativeMethodForArbitraryExpressions: String
  ): List[FieldInfo[A, B]] = ${
    fieldInfoRuleForMultipleMacroImpl[A, B]('propertyExtractors, 'alternativeMethodForArbitraryExpressions)
  }

  import scala.quoted.*

  private def fieldInfoMacroImpl[A: Type, B: Type](
    propertyExtractor: Expr[A => B],
    alternativeMethodForArbitraryExpressions: Expr[String]
  )(using Quotes): Expr[FieldInfo[A, B]] = {
    import quotes.reflect.*
    val unwrappedBlock = PropertyExtractionUtils.unwrapInlines(propertyExtractor.asTerm)
    val propertyName = PropertyExtractionUtils.extractPropertyNameFromBlockAST(
      unwrappedBlock,
      alternativeMethodForArbitraryExpressions.valueOrAbort
    )
    val propertyNameExpr = Expr(propertyName)
    '{ FieldInfo[A, B](${ propertyExtractor }, NestedFieldKey(${ propertyNameExpr })) }
  }

  private def fieldInfoRuleForMultipleMacroImpl[A: Type, B: Type](
    propertyExtractors: Expr[Seq[A => B]],
    alternativeMethodForArbitraryExpressions: Expr[String]
  )(using Quotes): Expr[List[FieldInfo[A, B]]] = {
    import quotes.reflect.*
    val unwrapped                                                      = PropertyExtractionUtils.unwrapInlines(propertyExtractors.asTerm)
    val Typed(Repeated(propertyExtractorsList: List[? <: Term], _), _) = unwrapped: @unchecked
    val fieldInfos = propertyExtractorsList.map { propertyExtractorTerm =>
      val propertyName = PropertyExtractionUtils.extractPropertyNameFromBlockAST(
        propertyExtractorTerm,
        alternativeMethodForArbitraryExpressions.valueOrAbort
      )
      val extractorExpression = propertyExtractorTerm.asExprOf[A => B]
      val propertyNameExpr    = Expr(propertyName)
      '{ FieldInfo[A, B](${ extractorExpression }, NestedFieldKey(${ propertyNameExpr })) }
    }
    Expr.ofList(fieldInfos)
  }

}
