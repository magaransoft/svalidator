package com.magaran.svalidator.validation.simple.internals

/** Provides an interface for retrieving stored values using [[LazyValueContainer]] to avoid
  * double initialization/extraction of properties when passing them down a dependent validation chain
  */
protected[internals] trait UpstreamLazyValueProvider[A]:

  /** Retrieves the stored value */
  protected[internals] def fetchValue(chainId: FieldValidationChainId): A
