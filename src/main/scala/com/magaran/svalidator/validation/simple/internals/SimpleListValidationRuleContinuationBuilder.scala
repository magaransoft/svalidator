package com.magaran.svalidator.validation.simple.internals

import scala.annotation.targetName
import scala.collection.mutable

import com.magaran.svalidator.validation.CanFormat
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey
import com.magaran.typedmap.TypedEntry
import org.jetbrains.annotations.PropertyKey

/** Builder for appending messages, metadata, adding conditional validation, mapping properties into something else, or
  * switching into other fields during a validation stream
  *
  * @tparam A Type of the instance being validated
  * @tparam B Type of the extracted property being validated
  * @tparam C If the property has been mapped, type the property had before the map, otherwise, [[scala.Nothing]]
  */
protected[internals] final class SimpleListValidationRuleContinuationBuilder[A, B, +C](
  fieldsInformation: Vector[SingleFieldInformationContainer[A, B, C]],
  currentRuleStructure: Option[SimpleValidationRuleStructureContainer[A, B]],
  validationExpressions: Vector[SimpleValidationRuleStructureContainer[A, B]],
  markIndexesOfFieldNameErrors: Boolean,
) extends RuleBuilderWithoutMessageSet[A]
    with UpstreamLazyValueProvider[Vector[B]]:

  /** Converts the extracted property of the preceding chain by applying <strong>once</strong> the function <code>f</code> only if all the preceding
    * validations are successful.  Further calls down the chain will work the type of the converted value.
    *
    * @param f Function to convert the extracted property
    * @tparam D The new type of the property chain
    */
  protected[internals] def map[D](
    f: B => D,
    wrappingBuilder: RuleBuilderWithMessageSet[A]
  ): SimpleListValidationRuleStarterBuilder[A, D, B] =
    val newFieldsInfo = fieldsInformation.map { fieldInformation =>
      SingleFieldInformationContainer(
        fieldInformation.propertyListExpression.andThen(_.map(f)),
        fieldInformation.fieldKey,
        Some(wrappingBuilder),
        Some(UpstreamValueProviderAndConverter(this, f)),
        fieldInformation.fieldValidationChainId
      )
    }
    SimpleListValidationRuleStarterBuilder[A, D, B](newFieldsInfo, None, Vector.empty, markIndexesOfFieldNameErrors)

  /** Switches into a different property on the same chained validation stream
    *
    * If any previous validations before switching field fail, any further statements will not be evaluated.
    *
    * @param propertyExpression The extractor for the new property to switch into
    * @tparam D The type of the new property
    */
  protected[internals] def andFor[D](
    propertyExpression: A => D,
    wrappingBuilder: RuleBuilderWithMessageSet[A]
  ): FieldListRequiringSimpleValidatorRuleBuilder[A, D] =
    val composedFunction: A => Seq[D] = x => Vector(propertyExpression(x))
    FieldListRequiringSimpleValidatorRuleBuilder(
      composedFunction,
      markIndexesOfErrors = false,
      dependsOnUpstream = Some(wrappingBuilder)
    )

  /** Switches the [[com.magaran.svalidator.FieldKey]] to use for validation errors from this point on.  The switch will be done at current
    * nesting level, if the current [[com.magaran.svalidator.FieldKey]] had any parents, it will be assumed the passed in field name resides
    * within the same parent.
    *
    * @param newFieldName  Field name that will be used for errors from this point on
    */
  protected[internals] def switchFieldTo(
    newFieldName: String,
    wrappingBuilder: RuleBuilderWithMessageSet[A]
  ): SimpleListValidationRuleMappableStarterBuilder[A, B, B] =

    val nextFieldsInformation = fieldsInformation.map { fieldInformation =>
      val keyToUse = fieldInformation.fieldKey match
        case f: NestedFieldKey => f.parent.downField(newFieldName)
        case FieldKey.Root     => NestedFieldKey(newFieldName)
      SingleFieldInformationContainer(
        propertyListExpression = fieldInformation.propertyListExpression,
        fieldKey = keyToUse,
        previousMappedBuilderInChain = Some(wrappingBuilder),
        previousMappedProviderAndConverter = Some(UpstreamValueProviderAndConverter(this, identity[B])),
        fieldValidationChainId = fieldInformation.fieldValidationChainId
      )
    }
    SimpleListValidationRuleMappableStarterBuilder[A, B, B](nextFieldsInformation, markIndexesOfFieldNameErrors)

  /** Switches the [[com.magaran.svalidator.FieldKey]] used for errors to the given key for any validations that occur further down the chain.
    *
    * @param fieldKey Field key that will be used for errors from this point on
    */
  protected[internals] def switchFieldTo(
    fieldKey: FieldKey,
    wrappingBuilder: RuleBuilderWithMessageSet[A]
  ): SimpleListValidationRuleMappableStarterBuilder[A, B, B] =
    val nextFieldsInformation = fieldsInformation.map { fieldInformation =>
      SingleFieldInformationContainer(
        fieldInformation.propertyListExpression,
        fieldKey,
        Some(wrappingBuilder),
        Some(UpstreamValueProviderAndConverter(this, identity[B])),
        fieldInformation.fieldValidationChainId
      )
    }
    SimpleListValidationRuleMappableStarterBuilder[A, B, B](nextFieldsInformation, markIndexesOfFieldNameErrors)

  /** Causes the preceding [[must]] or [[mustNot]] call to be applied only if the passed in condition evaluates to true
    *
    * @param conditionedValidation Condition to be applied to the instance
    */
  infix def when(conditionedValidation: (B, A) => Boolean): SimpleListValidationRuleWhenContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWhenContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure.map(_.copy(conditionalValidation = Some(conditionedValidation))),
        validationExpressions,
      )
    )

  /** Causes the preceding [[must]] or [[mustNot]] call to be applied only if the passed in condition evaluates to true
    *
    * @param conditionedValidation Condition to be applied to the instance
    */
  infix def when(conditionedValidation: B => Boolean): SimpleListValidationRuleWhenContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWhenContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure
          .map(_.copy(conditionalValidation = Some((property, _) => conditionedValidation(property)))),
        validationExpressions,
      )
    )

  /** Causes the preceding[[must]] or [[mustNot]] call to be applied only if the passed in condition evaluates to true
    *
    * @param condition Condition to be applied to the instance
    */
  infix def when(condition: => Boolean): SimpleListValidationRuleWhenContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWhenContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure.map(_.copy(conditionalValidation = Some((_, _) => condition))),
        validationExpressions,
      )
    )

  /** Assigns the messageKey for the preceding [[must]] or [[mustNot]] call.
    *
    * By default, the message will use the value of the property when it is formatted using
    * [[scala.collection.StringOps.format StringOps.format]].  If you'd like to use a custom list of values,
    * call [[withFormat(value:*]] in this chain.
    *
    * @param messageKey   The raw message or a key string for localized messages
    */
  infix def withMessage(
    @PropertyKey(resourceBundle = "messages") messageKey: String
  ): SimpleListValidationRuleWithMessageContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWithMessageContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure.map(_.copy(errorMessageKey = Some(messageKey))),
        validationExpressions,
      )
    )

  /** Assigns the  passed in value as the format value for the message of the [[withMessage]] in this chain
    *
    * @param value The value to use when formatting the message key
    */
  protected[internals] def withFormat(
    value: => CanFormat
  ): SimpleListValidationRuleWithFormatContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWithFormatContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure.map(_.copy(errorMessageFormatValues = Some((_, _) => Vector(value)))),
        validationExpressions,
      )
    )

  /** Assigns the elements of the passed in product as the format values for the message of the [[withMessage]] in this chain
    *
    * @param values The value to use when formatting the message key
    */
  @targetName("withFormatFromProduct")
  protected[internals] def withFormat(
    values: => Product
  ): SimpleListValidationRuleWithFormatContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWithFormatContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure.map(_.copy(errorMessageFormatValues = Some((_, _) => values.productIterator.toVector))),
        validationExpressions,
      )
    )

  /** Invokes the passed in  function with the property value and assigns the result as the format values for the message
    * of the [[withMessage]] in this chain
    *
    * @param argsFunction The value to use when formatting the message key
    */
  protected[internals] def withFormat(
    argsFunction: B => Seq[Any]
  ): SimpleListValidationRuleWithFormatContinuationBuilder[A, B, C] =
    SimpleListValidationRuleWithFormatContinuationBuilder(
      buildNextInstanceInChain(
        currentRuleStructure
          .map(_.copy(errorMessageFormatValues = Some((property, _) => argsFunction(property).toVector))),
        validationExpressions,
      )
    )

  /** Assigns the specified entry to the metadata of the resulting
    * [[com.magaran.svalidator.validation.ValidationFailure ValidationFailure]]
    * if the previous [[must]] or [[mustNot]] fails validation.
    *
    * It is suggested to use [[com.magaran.typedmap.TypedKey TypedKey]]'s [[com.magaran.typedmap.TypedKey.-> ->]] method
    * to create the argument.
    *
    * @param entry  Entry to add as metadata
    */
  protected[internals] def withMetadata[D](
    entry: TypedEntry[D]
  ): SimpleListValidationRuleWithMetadataContinuationBuilder[A, B, C] =
    val composedFunction: B => TypedEntry[D] = _ => entry
    val nextCurrentRuleStructure = currentRuleStructure.map { ruleStructure =>
      ruleStructure.copy(metadataFunctions = ruleStructure.metadataFunctions :+ composedFunction)
    }
    SimpleListValidationRuleWithMetadataContinuationBuilder(
      buildNextInstanceInChain(nextCurrentRuleStructure, validationExpressions)
    )

  /** Stores the specified function to generate the metadata of the resulting
    * [[com.magaran.svalidator.validation.ValidationFailure ValidationFailure]]
    * if the previous [[must]] or [[mustNot]] fails validation.  The function will receive the property value as an argument.
    *
    * It is suggested to use [[com.magaran.typedmap.TypedKey TypedKey]]'s [[com.magaran.typedmap.TypedKey.-> ->]] method
    * to create the return value within the function.
    *
    * @param entryFunction  The function that generates the entry to add as metadata
    */
  protected[internals] def withMetadata[D](
    entryFunction: B => TypedEntry[D]
  ): SimpleListValidationRuleWithMetadataContinuationBuilder[A, B, C] =
    val nextCurrentRuleStructure = currentRuleStructure.map { ruleStructure =>
      ruleStructure.copy(metadataFunctions = ruleStructure.metadataFunctions :+ entryFunction)
    }
    SimpleListValidationRuleWithMetadataContinuationBuilder(
      buildNextInstanceInChain(nextCurrentRuleStructure, validationExpressions)
    )

  protected[internals] override def fetchValue(chainId: FieldValidationChainId): Vector[B] = lazyExtractedPropertiesMap(
    chainId
  ).extractValue

  private val defaultConditionedValidation: (B, A) => Boolean = (_, _) => true

  private val lazyExtractedPropertiesMap: mutable.Map[FieldValidationChainId, LazyValueContainer[Vector[B]]] =
    mutable.Map.empty

  protected[internals] def buildNextInstanceInChain(
    currentRuleStructure: Option[SimpleValidationRuleStructureContainer[A, B]],
    validationExpressions: Vector[SimpleValidationRuleStructureContainer[A, B]],
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    SimpleListValidationRuleContinuationBuilder(
      fieldsInformation,
      currentRuleStructure,
      validationExpressions,
      markIndexesOfFieldNameErrors,
    )

  protected[internals] def internalMust(
    lazyBooleanValue: => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val composedFunction = (_: B) => lazyBooleanValue
    addRuleExpressionToList(composedFunction)

  protected[internals] def internalMust(
    ruleExpressionReceivingPropertyValue: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    addRuleExpressionToList(ruleExpressionReceivingPropertyValue)

  protected[internals] override def uncheckedBuildRules(instance: A): RuleStreamCollection[A] =
    val ruleStructures = currentRuleStructure match
      case None    => validationExpressions
      case Some(x) => validationExpressions :+ x
    processRuleStructures(instance, ruleStructures)

  private def processRuleStructures(
    instance: A,
    ruleStructuresList: Vector[SimpleValidationRuleStructureContainer[A, B]]
  ): RuleStreamCollection[A] =
    val chainsForProperties = fieldsInformation.map { fieldInformation =>
      val upstream: Option[RuleStreamCollection[A]] =
        fieldInformation.previousMappedBuilderInChain.map(_.buildRules(instance))
      lazy val lazyPropertyListValue = fieldInformation.previousMappedProviderAndConverter match
        case Some(UpstreamValueProviderAndConverter(provider, converter)) =>
          provider.fetchValue(fieldInformation.fieldValidationChainId).map(converter)
        case None => fieldInformation.propertyListExpression(instance).toVector

      val mainRuleStream = ruleStructuresList.to(LazyList) map: ruleStructureContainer =>
        val errorMessageKey = ruleStructureContainer.errorMessageKey match
          case Some(value) => value
          case None =>
            throw IllegalStateException(
              "Can not invoke uncheckedBuildRules without a message key set in every rule of the rule builder"
            )
        SingleFieldSimpleListValidationRule[A, B](
          lazyPropertyListValue,
          fieldInformation.fieldKey,
          ruleStructureContainer.validationExpression,
          errorMessageKey,
          ruleStructureContainer.errorMessageFormatValues,
          ruleStructureContainer.conditionalValidation.getOrElse(defaultConditionedValidation),
          markIndexesOfFieldNameErrors,
          ruleStructureContainer.metadataFunctions
        )
      lazyExtractedPropertiesMap.put(fieldInformation.fieldValidationChainId, LazyValueContainer(lazyPropertyListValue))
      ChainedValidationStream(Vector(mainRuleStream), upstream)
    }
    RuleStreamCollection(chainsForProperties)

  private def addRuleExpressionToList(
    ruleExpression: B => Boolean
  ): SimpleListValidationRuleContinuationBuilder[A, B, C] =
    val ruleVector = currentRuleStructure match
      case None                => validationExpressions
      case Some(ruleStructure) => validationExpressions :+ ruleStructure
    buildNextInstanceInChain(
      Some(SimpleValidationRuleStructureContainer[A, B](ruleExpression, None, None, None, Vector.empty)),
      ruleVector,
    )
