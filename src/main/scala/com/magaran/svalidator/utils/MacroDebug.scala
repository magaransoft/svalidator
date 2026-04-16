package com.magaran.svalidator.utils

import scala.quoted.*

/** Compile-time debugging utility that prints the AST representation of an expression during macro expansion. */
object MacroDebug {
  private def printImpl[A: Type](a: Expr[A])(using q: Quotes): Expr[Unit] = {
    import q.*, q.reflect.*
    println(a.asTerm.show(using Printer.TreeShortCode))
    '{}
  }

  inline def print[A](inline a: A): Unit = ${ printImpl('{ a }) }
}
