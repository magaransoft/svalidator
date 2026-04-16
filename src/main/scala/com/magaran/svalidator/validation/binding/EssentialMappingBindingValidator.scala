package com.magaran.svalidator.validation.binding

import scala.reflect.ClassTag

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.special.ObjectBinder
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingEventRegistry
import com.magaran.svalidator.binding.events.*
import com.magaran.svalidator.evidences.CanConvert
import com.magaran.svalidator.validation.simple.internals.EssentialValidator
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.ValidationResult
import com.magaran.svalidator.FieldKey

/** Base class for BindingValidator and BindingValidatorWithData, to provide maximum code reuse.
  *
  * @tparam A Type of the instance being bound
  * @tparam ValidationType Type of the validation result
  * @tparam BindingType Type of the binding result
  */
protected[svalidator] abstract class EssentialMappingBindingValidator[
  A,
  ValidationType <: ValidationResult,
  BindingType >: Failure[A] <: BindingAndValidationResult[A]
](using converter: CanConvert[A, ValidationType, BindingType])
    extends EssentialValidator[A, ValidationType]:

  /** Attempts to perform binding and validation of the given type using the specified json string.
    *
    * This method calls
    * ObjectBinder.[[com.magaran.svalidator.binding.binders.special.ObjectBinder.bind bind]] method to
    * perform the binding, and, if successful, applies <code>mapOp</code> to it, and then calls <code>validate</code>
    * on the transformed value. If not, field errors are converted to validation failures and returned in the summary
    *
    * @param jsonString      String to use for json binding
    * @param mapOp           The transformation function from the bound value's type to the validated value's type
    * @param context         Binding context information for this operation
    * @param config          Binding config to use during binding
    * @param localizer       Localizer for binding failure messages
    * @param tag             ClassTag for the bound type
    * @tparam C Type of the instance being bound
    * @return A summary of field errors or validation failures if any occurred, or a summary containing the bound instance
    *         otherwise.
    */
  def bindAndValidate[C: TypedBinder](
    jsonString: String,
    mapOp: C => A
  )(using BindingContext, BindingConfig, Localizer, ClassTag[C]): BindingType =
    val source = JsonCursor(jsonString).fold(identity, identity)
    bindAndValidate(source, mapOp)

  /** Attempts to perform binding and validation of the given type using the specified source.
    *
    * This method calls
    * ObjectBinder.[[com.magaran.svalidator.binding.binders.special.ObjectBinder.bind bind]] method to
    * perform the binding, and, if successful, applies <code>mapOp</code> to it, and then calls <code>validate</code>
    * on the transformed value. If not, field errors are converted to validation failures and returned in the summary
    *
    * @param source          Source to use for binding
    * @param mapOp           The transformation function from the bound value's type to the validated value's type
    * @param context         Binding context information for this operation
    * @param config          Binding config to use during binding
    * @param localizer       Localizer for binding failure messages
    * @param tag             ClassTag for the bound type
    * @tparam C Type of the instance being bound
    * @return A summary of field errors or validation failures if any occurred, or a summary containing the bound instance
    *         otherwise.
    */
  def bindAndValidate[C: TypedBinder](source: Source, mapOp: C => A)(using BindingContext, BindingConfig)(
    using localizer: Localizer,
    tag: ClassTag[C]
  ): BindingType =
    val registry = BindingEventRegistry
    registry.publishEvent(BeforeBindingAndValidationEvent(source, tag))
    val bindingResult = ObjectBinder.bind[C](source, FieldKey.Root)
    bindingResult match
      case f: BindingFailure =>
        registry.publishEvent(FailedBindingPreventedValidationEvent(f, tag))
        f.asFailure
      case p: BindingPass[C] =>
        val value = p.value
        registry.publishEvent(SuccessfulBindingBeforeValidationEvent(p, tag))
        val mappedValue    = mapOp(value)
        val validationType = validate(using mappedValue)
        val result         = converter.convert(mappedValue, p, validationType, localizer)
        result match
          case f: Failure[?]       => registry.publishEvent(SuccessfulBindingFailedValidationEvent(f, tag))
          case s: BoundAndValid[?] => registry.publishEvent(SuccessfulBindingAndValidationEvent(s, tag))
        result
