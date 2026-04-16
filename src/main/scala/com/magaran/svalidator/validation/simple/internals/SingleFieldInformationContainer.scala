package com.magaran.svalidator.validation.simple.internals

import com.magaran.svalidator.FieldKey

/** Holds the field extraction, key, and chain context for a single field being validated.
  * Used internally by the rule builder pipeline to track state across chained rule operations.
  *
  * @tparam A the instance type being validated
  * @tparam B the type of the property being validated
  * @tparam C the upstream mapped type, if any
  */
final class SingleFieldInformationContainer[A, B, C](
  val propertyListExpression: A => Seq[B],
  val fieldKey: FieldKey,
  val previousMappedBuilderInChain: Option[RuleBuilderWithMessageSet[A]],
  val previousMappedProviderAndConverter: Option[UpstreamValueProviderAndConverter[B, C]],
  val fieldValidationChainId: FieldValidationChainId
)
