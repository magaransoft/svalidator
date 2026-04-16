package com.magaran.svalidator.validation.simple.internals
import scala.quoted.*

import com.magaran.svalidator.utils.PropertyExtractionUtils

/** Compile-time macro implementations for the `For`, `ForOptional`, `ForEach`, `ForComponent`,
  * `ForOptionalComponent`, and `ForEachComponent` rule builder entry points.
  *
  * These macros extract property names from lambda expressions at compile time, enabling
  * the fluent `For(_.fieldName)` syntax without requiring an explicit field name string.
  */
object EssentialValidatorMacros {

  private def automaticFieldMacro[Q <: Quotes](
    using q: Q
  )(propertyExpression: Expr[Any], exampleAst: Expr[Any]): q.reflect.Term = {
    import quotes.reflect.*
    val sampleAstTerm                                                  = exampleAst.asTerm
    val Inlined(a, b, Apply(select, List(Literal(StringConstant(_))))) = sampleAstTerm: @unchecked
    val Select(Apply(TypeApply(Select(_, invokedMethod), _), _), _)    = select: @unchecked
    val propertyName =
      PropertyExtractionUtils.extractPropertyNameFromBlockAST(
        PropertyExtractionUtils.unwrapInlines(propertyExpression.asTerm),
        s"`${invokedMethod.toString.split("\\$").last}`"
      )
    val targetAst = Inlined(a, b, Apply(select, List(Literal(StringConstant(propertyName)))))
    targetAst
  }

  def ForMacro[A, B](
    propertyExpression: Expr[A => B],
    exampleAst: Expr[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]]
  )(using Quotes, Type[A], Type[B]): Expr[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]] = {
    automaticFieldMacro(propertyExpression, exampleAst)
      .asExprOf[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]]
  }

  def ForOptionalMacro[A, B](
    propertyExpression: Expr[A => Option[B]],
    exampleAst: Expr[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]]
  )(using Quotes, Type[A], Type[B]): Expr[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]] = {
    automaticFieldMacro(propertyExpression, exampleAst)
      .asExprOf[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]]
  }

  def ForEachMacro[A, B](
    propertyExpression: Expr[A => Seq[B]],
    exampleAst: Expr[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]]
  )(using Quotes, Type[A], Type[B]): Expr[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]] = {
    automaticFieldMacro(propertyExpression, exampleAst)
      .asExprOf[SimpleListValidationRuleMappableStarterBuilder[A, B, Nothing]]
  }

  def ForComponentMacro[A, B](
    propertyExpression: Expr[A => B],
    exampleAst: Expr[ComponentListValidationRuleBuilder[A, B]]
  )(using Quotes, Type[A], Type[B]): Expr[ComponentListValidationRuleBuilder[A, B]] = {
    automaticFieldMacro(propertyExpression, exampleAst)
      .asExprOf[ComponentListValidationRuleBuilder[A, B]]
  }

  def ForOptionalComponentMacro[A, B](
    propertyExpression: Expr[A => Option[B]],
    exampleAst: Expr[ComponentListValidationRuleBuilder[A, B]]
  )(using Quotes, Type[A], Type[B]): Expr[ComponentListValidationRuleBuilder[A, B]] = {
    automaticFieldMacro(propertyExpression, exampleAst)
      .asExprOf[ComponentListValidationRuleBuilder[A, B]]
  }

  def ForEachComponentMacro[A, B](
    propertyExpression: Expr[A => Seq[B]],
    exampleAst: Expr[ComponentListValidationRuleBuilder[A, B]]
  )(using Quotes, Type[A], Type[B]): Expr[ComponentListValidationRuleBuilder[A, B]] = {
    automaticFieldMacro(propertyExpression, exampleAst)
      .asExprOf[ComponentListValidationRuleBuilder[A, B]]
  }

}
