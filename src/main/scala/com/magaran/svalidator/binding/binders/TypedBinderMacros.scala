package com.magaran.svalidator.binding.binders

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

import com.magaran.svalidator.binding.binders.special.ProductBinder
import com.magaran.svalidator.binding.binders.special.StableEnumBinder

/** Compile-time macro implementations for auto-deriving [[TypedBinder]] instances.
  *
  * Supports deriving binders for case classes (via [[ProductBinder]]) and Scala 3 enums
  * with an `Int` identifier field (via [[StableEnumBinder]]). Derivation is recursive —
  * nested case classes are derived automatically if no explicit binder is in scope.
  */
protected[binders] object TypedBinderMacros:

  protected[binders] def performDerive[A: Type](using Quotes): Expr[TypedBinder[A]] = {
    import quotes.reflect.*
    Expr.summon[Mirror.Of[A]] match {
      case Some(mirrorExpr) =>
        mirrorExpr match
          case '{ ${ _ }: Mirror.ProductOf[A] { type MirroredElemTypes = elementTypes } } =>
            val labels  = getFieldNamesImpl[A]
            val binders = summonInstances[A, elementTypes]
            deriveProduct(labels, Expr.ofList(binders))
          case '{ ${ _ }: Mirror.SumOf[A] } => deriveStableEnum[A]
      case None =>
        report.errorAndAbort(
          s"Unable to auto-derive TypedBinder for ${Type.show[A]}: it must be a case class or an enum with an id:Int field"
        )
    }
  }

  private def deriveProduct[A](fieldNames: Expr[List[String]], fieldBinders: Expr[List[TypedBinder[?]]])(
    using Quotes,
    Type[A]
  ): Expr[TypedBinder[A]] =
    import quotes.reflect.*
    val typeRepr    = TypeRepr.of[A]
    val classSymbol = typeRepr.typeSymbol
    val companion   = classSymbol.companionModule

    val fromProductMethod = Select.unique(Ref(companion), "fromProduct").etaExpand(Symbol.spliceOwner)
    if (!classSymbol.typeMembers.exists(_.isTypeParam)) {
      val fromProductMethodExpr = fromProductMethod.asExprOf[Product => A]
      '{
        new ProductBinder[A](${ fromProductMethodExpr }, ${ fieldNames }, ${ fieldBinders })
      }
    } else {
      val fromProductMethodExpr = fromProductMethod.asExprOf[Product => Any]
      '{
        new ProductBinder[A](x => ${ fromProductMethodExpr }(x).asInstanceOf[A], ${ fieldNames }, ${ fieldBinders })
      }
    }

  private def deriveStableEnum[T: Type](using Quotes): Expr[TypedBinder[T]] =
    import quotes.reflect.*
    val tpe = TypeRepr.of[T]

    val classSymbol = tpe.classSymbol
    val sym = classSymbol match
      case Some(sym) if sym.flags.is(Flags.Enum) && !sym.flags.is(Flags.JavaDefined) => sym
      case _                                                                         => report.errorAndAbort(s"${tpe.show} is not an enum type")

    val companion             = sym.companionModule
    val valuesCompanionMethod = companion.declaredMethod("values").headOption
    valuesCompanionMethod match
      case Some(_) =>
      case None    => report.errorAndAbort(s"${tpe.show} does not have a values method")

    val typeSymbol              = tpe.typeSymbol
    val constructorValueParams  = typeSymbol.primaryConstructor.paramSymss.lastOption.getOrElse(Nil)
    val leadingConstructorParam = constructorValueParams.headOption
    val leadingConstructorParamName = leadingConstructorParam match
      case Some(param) =>
        param.name
      case None => report.errorAndAbort(s"${tpe.show} does not have a leading constructor param")

    val identifierField = typeSymbol.fieldMember(leadingConstructorParamName)
    identifierField match
      case target if target.isNoSymbol =>
        report.errorAndAbort(
          s"${tpe.show} does not have a public Int field with the same name as its leading constructor param"
        )
      case idMethod =>
        val targetIsAnInt = tpe.memberType(idMethod) =:= TypeRepr.of[Int]
        if !targetIsAnInt then
          report.errorAndAbort(
            s"${tpe.show} the public field with the same name as the leading constructor param field is not an int"
          )

    val valuesRef = Select.unique(Ref(companion), "values").asExprOf[Array[T]]
    '{ new StableEnumBinder[T](${ valuesRef }) }
  end deriveStableEnum

  private def getFieldNamesImpl[T: Type](using Quotes): Expr[List[String]] = {
    import quotes.reflect.*
    val tpe        = TypeRepr.of[T]
    val fieldNames = tpe.typeSymbol.caseFields.map(_.name)
    Expr(fieldNames)
  }

  private def summonInstances[RootType: Type, Elems: Type](using Quotes): List[Expr[TypedBinder[?]]] =
    Type.of[Elems] match
      case '[elem *: elems] => deriveOrSummon[RootType, elem] :: summonInstances[RootType, elems]
      case '[EmptyTuple]    => Nil

  private def deriveOrSummon[T: Type, Elem: Type](using Quotes): Expr[TypedBinder[Elem]] =
    Type.of[Elem] match
      case '[T] => deriveRec[T, Elem]
      case _    => '{ summonInline[TypedBinder[Elem]] }

  private def deriveRec[T: Type, Elem: Type](using Quotes): Expr[TypedBinder[Elem]] =
    import quotes.reflect.*
    Type.of[T] match
      case '[Elem] =>
        report.errorAndAbort(s"Infinite recursive derivation detected while deriving TypedBinder[${Type.show[Elem]}]")
      case _ => performDerive[Elem] // recursive derivation

end TypedBinderMacros
