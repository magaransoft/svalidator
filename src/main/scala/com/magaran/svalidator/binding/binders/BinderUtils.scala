package com.magaran.svalidator.binding.binders

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey

/** Contains utility functions used when binding */
object BinderUtils:

  /** Block wrapper to use when the target type being bound can not be bound at [[FieldKey.Root]] level.
    *
    * @param source The [[Source]] that will provide values for binding
    * @param fieldKey [[FieldKey]] of the field being bound
    * @param block The block to execute if the field key is not <code>FieldKey.Root</code>
    * @return A [[BindingFailure]] with the message provided by [[com.magaran.svalidator.binding.config.BindingLanguageConfig.invalidRootBindingOfType BindingLanguageConfig.invalidRootBindingOfType]]
    *         if the passed in <code>fieldKey</code> is <code>Root</code>, otherwise, the block's return value.
    */
  def nonRootBinding[A](source: Source, fieldKey: FieldKey)(
    block: NestedFieldKey => BindingResult[A]
  )(using BindingConfig, BindingContext, TypeShow[A]): BindingResult[A] =
    fieldKey match
      case FieldKey.Root =>
        BindingFailure(
          fieldKey,
          summon[BindingLanguageConfig].invalidRootBindingOfType(summon[TypeShow[A]].targetTypeName),
          None
        )(using source)
      case nestedKey: NestedFieldKey => block(nestedKey)
