package com.magaran.svalidator.validation.simple.internals

/** Builder that applies a boolean function to validate an extracted property, and gives the option of a single initial
  * mapping of the property if necessary.
  *
  * @tparam A Type of the instance being validated
  * @tparam B Type of the extracted property being validated
  * @tparam C If the property has been mapped, type the property had before the map, otherwise, [[scala.Nothing Nothing]]
  */
protected[internals] class SimpleListValidationRuleMappableStarterBuilder[A, B, +C](
  fieldsInformation: Vector[SingleFieldInformationContainer[A, B, C]],
  markIndexesOfFieldNameErrors: Boolean,
) extends SimpleListValidationRuleStarterBuilder[A, B, C](
      fieldsInformation,
      None,
      Vector.empty,
      markIndexesOfFieldNameErrors,
    ):

  /** Converts the extracted property of the preceding chain by applying <strong>once</strong> the function <code>f</code> only if all the preceding
    * validations are successful.  Further calls down the chain will work the type of the converted value.
    *
    * @param f Function to convert the extracted property
    * @tparam D The new type of the property chain
    */
  infix def map[D](f: B => D): SimpleListValidationRuleStarterBuilder[A, D, B] =
    val nextFieldsInformation = fieldsInformation.map { fieldInformation =>
      val nextProviderAndConverterOption = fieldInformation.previousMappedProviderAndConverter.map {
        case UpstreamValueProviderAndConverter(provider, converter) =>
          val nextProvider = new UpstreamLazyValueProvider[Vector[B]] {
            protected[internals] override def fetchValue(chainId: FieldValidationChainId): Vector[B] =
              provider.fetchValue(chainId).map(converter)
          }
          UpstreamValueProviderAndConverter(nextProvider, f)
      }
      SingleFieldInformationContainer(
        fieldInformation.propertyListExpression.andThen(_.map(f)),
        fieldInformation.fieldKey,
        fieldInformation.previousMappedBuilderInChain,
        nextProviderAndConverterOption,
        fieldInformation.fieldValidationChainId
      )
    }
    SimpleListValidationRuleStarterBuilder[A, D, B](
      nextFieldsInformation,
      None,
      Vector.empty,
      markIndexesOfFieldNameErrors,
    )
