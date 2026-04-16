package com.magaran.svalidator.validation.simple.internals

import com.magaran.typedmap.TypedEntry

protected[internals] final class SimpleListValidationRuleWithFormatContinuationBuilder[A, B, +C](
  wrapped: SimpleListValidationRuleContinuationBuilder[A, B, C]
) extends ContinuationRuleBuilderWithMessageSet[A, B, C](wrapped):

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
