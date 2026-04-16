package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.FieldKey

/** Binds [[scala.Option]]s of a given type, using the provided binder for the underlying type. */
final class OptionBinder[A](using TypedBinder[A]) extends TypedBinder[Option[A]]:

  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[Option[A]] =
    summon[TypedBinder[A]].bind(source, fieldKey) match
      case BindingPass(value) => BindingPass(Option(value))(using source)
      case failure: BindingFailure =>
        failure.cause match
          case Some(x) if x.isInstanceOf[NoSuchElementException] => BindingPass(None)(using source)
          case _                                                 => failure
