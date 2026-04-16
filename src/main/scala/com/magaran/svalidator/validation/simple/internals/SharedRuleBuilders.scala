package com.magaran.svalidator.validation.simple.internals

import scala.Conversion.into

import com.magaran.svalidator.validation.FieldInfo
import com.magaran.svalidator.validation.Invalid
import com.magaran.svalidator.validation.ResultWithSuccessData
import com.magaran.svalidator.validation.ResultWithoutData
import com.magaran.svalidator.validation.Valid
import com.magaran.svalidator.validation.ValidationFailure
import com.magaran.svalidator.FieldKey

/** Trait for shared rule builders between essential/simple and functional validators. */
trait SharedRuleBuilders[Instance] {

  export SharedRuleBuilders.*

  // noinspection UnitMethodIsParameterless
  given Unit = ()
  /* This unit is given to help as the default context when no context is required.
   *
   * Since scala has only value for the Unit type, any time a Unit is received as a given, this is the only possible
   * value anyway.
   */

  protected[validation] def applyRulesToInstance(
    ruleBuilders: Seq[RuleBuilderWithMessageSet[Instance]],
    instance: Instance
  ): List[ValidationFailure] = {
    val ruleStreamCollections = ruleBuilders.toList.map(_.buildRules(instance))
    ruleStreamCollections.flatMap { collection =>
      processRuleStreamCollection(instance, collection)
    }
  }

  /** Generates a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] that will only apply if
    * the given condition is true.
    *
    * @param conditionalExpression Condition to test
    * @return A continuation builder that will receive the [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]]s to apply if the condition is true.
    */
  protected final def When(
    conditionalExpression: Instance => Boolean
  ): ConditionedGroupValidationRuleBuilder[Instance] =
    ConditionedGroupValidationRuleBuilder(conditionalExpression)

  inline protected final def Field[C](inline propertyExtractor: Instance => C): FieldInfo[Instance, C] =
    FieldInfo(propertyExtractor, "`FieldInfo`s constructor")

  inline protected final def Key(inline propertyExtractor: Instance => Any): FieldKey = FieldKey.of(propertyExtractor)

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified property expression, using the name of the property as the field name for messages.  The expression must be a simple property selector (e.g.: _.myProperty or x => x.myProperty)
    * otherwise this macro will fail to expand.
    *
    * If you need to use an arbitrary expression,
    * use [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleFor RuleFor]] instead.
    *
    * @param propertySelector Extractor for the property to be validated
    */
  inline protected final def For[C](
    inline propertySelector: Instance => C
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] = ${
    EssentialValidatorMacros.ForMacro[Instance, C](
      'propertySelector,
      '{ RuleFor { propertySelector } ForField "fieldName" }
    )
  }

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified property expression.
    *
    * @param expression Extractor for the value to be validated
    * @return The continuation builder that will require the property name
    */
  protected final def RuleFor[C](expression: Instance => C): FieldListRequiringSimpleValidatorRuleBuilder[Instance, C] =
    val composedFunction: Instance => Seq[C] = x => Vector(expression(x))
    FieldListRequiringSimpleValidatorRuleBuilder[Instance, C](
      composedFunction,
      markIndexesOfErrors = false,
      dependsOnUpstream = None
    )

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified properties.
    *
    * @param fieldExtractor Field to be validated with the same rules
    * @param moreExtractors Additional fields to be validated with the same rules
    * @return
    */
  inline protected final def ForMultiple[C](
    inline fieldExtractor: Instance => C,
    inline moreExtractors: Instance => C*
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] = {
    RuleForMultiple(
      FieldInfo.apply(fieldExtractor, "`RuleForMultiple`"),
      FieldInfo.applySeq(moreExtractors, "`RuleForMultiple`")*
    )
  }

  inline protected final def ForMultipleOptional[C](
    inline fieldExtractor: Instance => Option[C],
    inline moreExtractors: Instance => Option[C]*
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] = {
    RuleForMultipleOptional(
      FieldInfo.apply(fieldExtractor, "`RuleForMultipleOptional`"),
      FieldInfo.applySeq(moreExtractors, "`RuleForMultipleOptional`")*
    )
  }

  inline protected final def ForMultipleEach[C](
    inline fieldExtractor: Instance => Seq[C],
    inline moreExtractors: Instance => Seq[C]*
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] = {
    RuleForMultipleEach(
      FieldInfo.apply(fieldExtractor, "`RuleForMultipleEach`"),
      FieldInfo.applySeq(moreExtractors, "`RuleForMultipleEach`")*
    )
  }

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified properties.
    *
    * @param moreFields The fields to be validated with the same rules
    * @return
    */
  protected final def RuleForMultiple[C](
    field: FieldInfo[Instance, C],
    moreFields: FieldInfo[Instance, C]*
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] =
    val fieldsInformation = moreFields
      .prepended(field)
      .map { field =>
        val composedFunction: Instance => Seq[C] = x => Vector(field.extractor(x))
        SingleFieldInformationContainer(
          propertyListExpression = composedFunction,
          fieldKey = field.fieldKey,
          previousMappedBuilderInChain = None,
          previousMappedProviderAndConverter = None,
          fieldValidationChainId = FieldValidationChainId()
        )
      }
      .toVector
    SimpleListValidationRuleMappableStarterBuilder(fieldsInformation, markIndexesOfFieldNameErrors = false)

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified properties, treating each one as if they were called using [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForOptional RuleForOptional]] and applied the same rules.
    */
  protected final def RuleForMultipleOptional[C](
    field: FieldInfo[Instance, Option[C]],
    moreFields: FieldInfo[Instance, Option[C]]*
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] =
    val fieldsInformation = moreFields
      .prepended(field)
      .map { field =>
        val composedFunction: Instance => Seq[C] = x => field.extractor(x).toVector
        SingleFieldInformationContainer(
          propertyListExpression = composedFunction,
          fieldKey = field.fieldKey,
          previousMappedBuilderInChain = None,
          previousMappedProviderAndConverter = None,
          fieldValidationChainId = FieldValidationChainId()
        )
      }
      .toVector
    SimpleListValidationRuleMappableStarterBuilder(fieldsInformation, markIndexesOfFieldNameErrors = false)

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified properties, treating each one as if they were called using
    * [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForEach RuleForEach]]
    * and applied the same rules.
    */
  protected final def RuleForMultipleEach[C](
    field: FieldInfo[Instance, Seq[C]],
    moreFields: FieldInfo[Instance, Seq[C]]*
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] =
    val fieldsInformation = moreFields
      .prepended(field)
      .map { field =>
        val composedFunction: Instance => Seq[C] = x => field.extractor(x)
        SingleFieldInformationContainer(
          propertyListExpression = composedFunction,
          fieldKey = field.fieldKey,
          previousMappedBuilderInChain = None,
          previousMappedProviderAndConverter = None,
          fieldValidationChainId = FieldValidationChainId()
        )
      }
      .toVector
    SimpleListValidationRuleMappableStarterBuilder(fieldsInformation, markIndexesOfFieldNameErrors = true)

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified list property, using the name of the property as the field name for messages. The expression must be a simple property selector (e.g.: _.myProperty or x => x.myProperty)
    * otherwise this macro will fail to expand.
    *
    * Any validations will be applied to each element and errors will be indexed with square brackets with the property name (
    * e.g.: myProperty[3])
    *
    * If you need to use an arbitrary expression, use [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForEach RuleForEach]] instead.
    *
    * @param propertySelector Extractor for the list of values to be validated
    */
  inline protected final def ForEach[C](
    inline propertySelector: Instance => Seq[C]
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] = ${
    EssentialValidatorMacros.ForEachMacro[Instance, C](
      'propertySelector,
      '{ RuleForEach { propertySelector } ForField "fieldName" }
    )
  }

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet]] for the
    * specified list property.
    *
    * Any validations will be applied to each element and errors will be indexed with square brackets with the property name (
    * e.g.: myProperty[3])
    *
    * @param expression Extractor for the list of values to be validated
    * @return The continuation builder that will require the property name
    */
  protected final def RuleForEach[C](
    expression: Instance => Seq[C]
  ): FieldListRequiringSimpleValidatorRuleBuilder[Instance, C] =
    FieldListRequiringSimpleValidatorRuleBuilder[Instance, C](
      expression,
      markIndexesOfErrors = true,
      dependsOnUpstream = None
    )

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet RuleBuilder]] for the
    * specified optional property, using the name of the property as the field name for messages.  The expression must be a simple property selector (e.g.: _.myProperty or x => x.myProperty)
    * otherwise this macro will fail to expand.
    *
    * Any validations will only be applied to if the option is defined.
    *
    * If you need to use an arbitrary expression,
    * use [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForOptional RuleForOptional]] instead.
    *
    * @param propertySelector Extractor for the optional property to be validated
    */
  inline protected final def ForOptional[C](
    inline propertySelector: Instance => Option[C]
  ): SimpleListValidationRuleMappableStarterBuilder[Instance, C, Nothing] = ${
    EssentialValidatorMacros.ForOptionalMacro[Instance, C](
      'propertySelector,
      '{ RuleForOptional { propertySelector } ForField "fieldName" }
    )
  }

  /** Starts a chain to generate a [[com.magaran.svalidator.validation.simple.internals.RuleBuilderWithMessageSet RuleBuilder]] for the
    * specified optional property.
    *
    * Any validations will only be applied if the option is defined.
    *
    * @param expression Extractor for the optional property to be validated
    * @return The continuation builder that will require the property name
    */
  protected final def RuleForOptional[C](
    expression: Instance => Option[C]
  ): FieldListRequiringSimpleValidatorRuleBuilder[Instance, C] =
    val composedFunction: Instance => Seq[C] = x => expression(x).iterator.toVector
    FieldListRequiringSimpleValidatorRuleBuilder[Instance, C](
      composedFunction,
      markIndexesOfErrors = false,
      dependsOnUpstream = None
    )

  /** Starts a chain to delegate validation of a component to an external [[com.magaran.svalidator.validation.Validator Validator]],
    * using the name of the property as the field name for messages. The component property must be a simple property
    * selector (e.g.: _.myProperty or x => x.myProperty), otherwise this macro will fail to expand.
    *
    * The results of the delegated validator will have the property name plus a dot prepended to their field names, and merged with
    * the results of the current validator (e.g.: myComponent.someFieldName).
    *
    * If you need to use an arbitrary expression,
    * use [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForComponent RuleForComponent]] instead.
    *
    * @param propertySelector Extractor for the component property to be validated
    */
  inline protected final def ForComponent[C](
    inline propertySelector: Instance => C
  ): ComponentListValidationRuleBuilder[Instance, C] = ${
    EssentialValidatorMacros.ForComponentMacro[Instance, C](
      'propertySelector,
      '{ RuleForComponent { propertySelector } ForField "fieldName" }
    )
  }

  /** Starts a chain to delegate validation of a component to an external [[com.magaran.svalidator.validation.Validator Validator]]
    *
    * The results of the delegated validator will have the property name plus a dot prepended to their field names, and merged with
    * the results of the current validator (e.g.: myComponent.someFieldName).
    *
    * @param componentPropertyExpression Extractor for the component property to be validated
    * @return The continuation builder that will require the property name
    */
  protected final def RuleForComponent[C](
    componentPropertyExpression: Instance => C
  ): ComponentListFieldRequiringSimpleValidatorRuleBuilder[Instance, C] =
    val composedFunction: Instance => Seq[C] = x => Vector(componentPropertyExpression(x))
    ComponentListFieldRequiringSimpleValidatorRuleBuilder[Instance, C](composedFunction, false)

  /** Starts a chain to delegate validation of a list of components to an external [[com.magaran.svalidator.validation.Validator Validator]],
    * using the name of the property as the field name for messages. The component property must be a simple property
    * selector (e.g.: _.myProperty or x => x.myProperty), otherwise this macro will fail to expand.
    *
    * The results of the delegated validator will have the property name of the component indexed by the position of the
    * validated component with square brackets followed by the field name(e.g.: myComponent[3].someFieldName) and merged
    * with the results of the current validator.
    *
    * If you need to use an arbitrary expression,
    * use [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForEachComponent RuleForEachComponent]] instead.
    *
    * @param propertySelector Extractor for the list of components to be validated
    */
  inline protected final def ForEachComponent[C](
    inline propertySelector: Instance => Seq[C]
  ): ComponentListValidationRuleBuilder[Instance, C] = ${
    EssentialValidatorMacros.ForEachComponentMacro[Instance, C](
      'propertySelector,
      '{ RuleForEachComponent { propertySelector } ForField "fieldName" }
    )
  }

  /** Starts a chain to delegate validation of a list of components to an external [[com.magaran.svalidator.validation.Validator Validator]]
    *
    * The results of the delegated validator will have the property name of the component indexed by the position of the
    * validated component with square brackets followed by the field name(e.g.: myComponent[3].someFieldName) and merged
    * with the results of the current validator .
    *
    * @param componentListPropertyExpression Extractor for the component property list to be validated
    * @return The continuation builder that will require the component property name
    */
  protected final def RuleForEachComponent[C](
    componentListPropertyExpression: Instance => Seq[C]
  ): ComponentListFieldRequiringSimpleValidatorRuleBuilder[Instance, C] =
    ComponentListFieldRequiringSimpleValidatorRuleBuilder[Instance, C](componentListPropertyExpression, true)

  /** Starts a chain to delegate validation of an optional component to an external [[com.magaran.svalidator.validation.Validator Validator]],
    * using the name of the property as the field name for messages. The component property must be a simple property
    * selector (e.g.: _.myProperty or x => x.myProperty), otherwise this macro will fail to expand.
    *
    * The results of the delegated validator will have the property name plus a dot prepended to their field names, and merged with
    * the results of the current validator (e.g.: myComponent.someFieldName). Validations will only be applied if the
    * component is present.
    *
    * If you need to use an arbitrary expression,
    * use [[com.magaran.svalidator.validation.simple.SimpleValidatorWithData.RuleForOptionalComponent RuleForOptionalComponent]] instead.
    *
    * @param propertySelector Extractor for the optional component to be validated
    */
  inline protected final def ForOptionalComponent[C](
    inline propertySelector: Instance => Option[C]
  ): ComponentListValidationRuleBuilder[Instance, C] = ${
    EssentialValidatorMacros.ForOptionalComponentMacro[Instance, C](
      'propertySelector,
      '{ RuleForOptionalComponent { propertySelector } ForField "fieldName" }
    )
  }

  /** Starts a chain to delegate validation of an optional component to an external [[com.magaran.svalidator.validation.Validator Validator]]
    *
    * The results of the delegated validator will have the property name plus a dot prepended to their field names, and merged with
    * the results of the current validator (e.g.: myComponent.someFieldName). Validations will only be applied if the
    * component is present.
    *
    * @param optionalComponentPropertyExpression Extractor for the optional component to be validated
    * @return The continuation builder that will require the component property name
    */
  protected final def RuleForOptionalComponent[C](
    optionalComponentPropertyExpression: Instance => Option[C]
  ): ComponentListFieldRequiringSimpleValidatorRuleBuilder[Instance, C] =
    val composedFunction: Instance => Seq[C] = x => optionalComponentPropertyExpression(x).iterator.toVector
    ComponentListFieldRequiringSimpleValidatorRuleBuilder[Instance, C](composedFunction, false)

  protected[validation] def processRuleStreamCollection(
    instance: Instance,
    collection: RuleStreamCollection[Instance]
  ): Vector[ValidationFailure] =
    collection.chains.flatMap { chain =>
      val upstreamResults =
        chain.dependsOnUpstream.map(processRuleStreamCollection(instance, _)).getOrElse(Vector.empty)
      if upstreamResults.isEmpty then
        chain.mainStream.flatMap { ruleStream =>
          ruleStream map { _.apply(instance) } collectFirst {
            case failures if failures.nonEmpty => failures
          } getOrElse Vector.empty
        }
      else upstreamResults
    }

}

object SharedRuleBuilders {

  /** Extension methods for converting and/or merging Either[Invalid, A] to ResultWithoutData or ResultWithSuccessData[A] */
  extension [A](source: Either[Invalid, A]) {

    def toResultWithoutData: ResultWithoutData =
      source match
        case Right(_)    => Valid
        case Left(error) => error

    def toResultWithData: ResultWithSuccessData[A] =
      source match
        case Right(data)   => Valid(data)
        case Left(invalid) => invalid

    def mergeUsing[B](fun: A => into[Either[Invalid, B]]): Either[Invalid, B] = {
      source match {
        case Right(value) => fun(value)
        case Left(value)  => Left(value)
      }
    }

    def mergeWith(another: ResultWithoutData): Either[Invalid, A] =
      source match
        case Right(data) =>
          another match
            case Valid      => Right(data)
            case x: Invalid => Left(x)
        case Left(invalid) =>
          another match
            case Valid      => Left(invalid)
            case y: Invalid => Left(invalid.merge(y))

    def mergeWith[B](another: ResultWithSuccessData[B]): Either[Invalid, (A, B)] =
      source match
        case Right(dataA) =>
          another match
            case Valid(dataB) => Right((dataA, dataB))
            case x: Invalid   => Left(x)
        case Left(invalid) =>
          another match
            case Valid(_)   => Left(invalid)
            case y: Invalid => Left(invalid.merge(y))

  }

}
