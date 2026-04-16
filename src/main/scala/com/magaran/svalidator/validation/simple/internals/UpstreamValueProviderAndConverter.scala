package com.magaran.svalidator.validation.simple.internals

/** Pairs a lazy value provider with a conversion function, used to chain mapped validations
  * where a downstream rule depends on values produced by an upstream rule in the same chain.
  *
  * @tparam CurrentPropertyType the type after conversion
  * @tparam UpstreamPropertyType the type provided by the upstream rule
  */
final case class UpstreamValueProviderAndConverter[CurrentPropertyType, UpstreamPropertyType](
  previousMappedBuilderValueProvider: UpstreamLazyValueProvider[Vector[UpstreamPropertyType]],
  previousMappedBuilderValueConverter: UpstreamPropertyType => CurrentPropertyType
)
