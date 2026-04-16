package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.NestedFieldKey
import io.circe.Decoder

/** Base class for binders whose target type can be directly decoded from a Json source
  *
  * @param convertFromString Function used to bind the target value from a <code>String</code>
  * @param invalidMessageFunction Function from [[com.magaran.svalidator.binding.config.BindingLanguageConfig]] to use to report error messages of the type
  * @tparam A The target type this binder wants to bind
  */
abstract class PrimitiveBinder[A: { Decoder, TypeShow }](
  convertFromString: String => A,
  invalidMessageFunction: BindingLanguageConfig => (NestedFieldKey, String) => MessageParts
) extends EssentialNonRootBinder[A, A](convertFromString, identity, invalidMessageFunction) {}
