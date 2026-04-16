package com.magaran.svalidator.utils

/** Captures the fully-qualified type name of `A` at compile time, for use in error messages and diagnostics.
  *
  * @param targetTypeName the string representation of the type
  */
final class TypeShow[A](val targetTypeName: String)

/** Companion providing an auto-derived `given` instance that resolves the type name via a macro. */
object TypeShow {

  import scala.quoted.*

  inline given [A]: TypeShow[A] = ${ fullTypeNameImpl[A] }

  private def fullTypeNameImpl[A: Type](using Quotes): Expr[TypeShow[A]] = {
    import quotes.reflect.*
    val typeRepr   = TypeRepr.of[A].dealias
    val typeSymbol = typeRepr.typeSymbol
    if (typeSymbol.isTypeParam || typeRepr.typeArgs.map(_.typeSymbol).exists(_.isTypeParam)) {
      report.errorAndAbort(
        s"No TypeShow found for ${typeRepr.show}, unable to synthesize it since it is a type argument or contains type arguments"
      )
    }
    val expr = Expr(typeRepr.show)
    '{ TypeShow[A](${ expr }) }
  }
}
