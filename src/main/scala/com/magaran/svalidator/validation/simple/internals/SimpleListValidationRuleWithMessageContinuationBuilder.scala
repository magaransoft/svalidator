package com.magaran.svalidator.validation.simple.internals

import scala.annotation.targetName

import com.magaran.svalidator.validation.CanFormat
import com.magaran.typedmap.TypedEntry

protected[internals] final class SimpleListValidationRuleWithMessageContinuationBuilder[A, B, +C](
  wrapped: SimpleListValidationRuleContinuationBuilder[A, B, C]
) extends ContinuationRuleBuilderWithMessageSet[A, B, C](wrapped):

  /** Assigns the  passed in value as the format value for the message of the [[SimpleListValidationRuleContinuationBuilder.withMessage withMessage]] in this chain
    *
    * @param value The value to use when formatting the message key
    */
  infix def withFormat(value: => CanFormat): SimpleListValidationRuleWithFormatContinuationBuilder[A, B, C] =
    wrapped.withFormat(value)

  /** Assigns the elements of the passed in product as the format values for the message of the [[SimpleListValidationRuleContinuationBuilder.withMessage withMessage]] in this chain
    *
    * @param values The value to use when formatting the message key
    */
  @targetName("withFormatFromProduct")
  infix def withFormat(values: => Product): SimpleListValidationRuleWithFormatContinuationBuilder[A, B, C] =
    wrapped.withFormat(values)

  /** Invokes the passed in  function with the property value and assigns the result as the format values for the message
    * of the [[SimpleListValidationRuleContinuationBuilder.withMessage withMessage]] in this chain
    *
    * @param argsFunction The value to use when formatting the message key
    */
  infix def withFormat(argsFunction: B => Seq[Any]): SimpleListValidationRuleWithFormatContinuationBuilder[A, B, C] =
    wrapped.withFormat(argsFunction)

  /** Assigns the specified entry to the metadata of the resulting
    * [[com.magaran.svalidator.validation.ValidationFailure ValidationFailure]]
    * if the previous [[must]] or [[mustNot]] fails validation.
    *
    * It is suggested to use [[com.magaran.typedmap.TypedKey TypedKey]]'s [[com.magaran.typedmap.TypedKey.-> ->]] method
    * to create the argument.
    *
    * @param entry Entry to add as metadata
    */
  infix def withMetadata[D](entry: TypedEntry[D]): SimpleListValidationRuleWithMetadataContinuationBuilder[A, B, C] =
    wrapped.withMetadata(entry)

  /** Stores the specified function to generate the metadata of the resulting
    * [[com.magaran.svalidator.validation.ValidationFailure ValidationFailure]]
    * if the previous [[must]] or [[mustNot]] fails validation.  The function will receive the property value as an argument.
    *
    * It is suggested to use [[com.magaran.typedmap.TypedKey TypedKey]]'s [[com.magaran.typedmap.TypedKey.-> ->]] method
    * to create the return value within the function.
    *
    * @param entryFunction  The function that generates the entry to add as metadata
    */
  infix def withMetadata[D](
    entryFunction: B => TypedEntry[D]
  ): SimpleListValidationRuleWithMetadataContinuationBuilder[A, B, C] =
    wrapped.withMetadata(entryFunction)
