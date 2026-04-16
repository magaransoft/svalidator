package com.magaran.svalidator.binding.events

import scala.reflect.ClassTag

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.exceptions.IllegalJsonCursorException
import com.magaran.svalidator.validation.binding.BoundAndValid
import com.magaran.svalidator.validation.binding.Failure
import com.magaran.svalidator.FieldKey

/** Base trait for all binding events */
sealed trait BindingEvent

/** Event that is fired before binding is attempted */
sealed class BeforeBindingEvent(val source: Source, val fieldKey: FieldKey, val tag: ClassTag[?]) extends BindingEvent

/** Event that is fired when parsing a json string to form a JsonCursor source, but the string is not valid json */
sealed class FailedJsonParsingEvent(val info: IllegalJsonCursorException) extends BindingAndValidationEvent

/** Event that is fired when binding fails */
sealed class FailedBindingEvent(val failure: BindingFailure, val tag: ClassTag[?]) extends BindingAndValidationEvent

/** Event that is fired when binding succeeds */
sealed class SuccessfulBindingEvent(val result: BindingPass[?], val tag: ClassTag[?]) extends BindingAndValidationEvent

/** Base trait for all binding and validation events in tandem, done by BindingValidators */
sealed trait BindingAndValidationEvent extends BindingEvent

/** Event that is fired before binding and validation is attempted, before the binding phase */
final class BeforeBindingAndValidationEvent(source: Source, tag: ClassTag[?])
    extends BeforeBindingEvent(source, FieldKey.Root, tag)
    with BindingAndValidationEvent

/** Event that is fired when binding phase failed in a BindingValidator.  Validation phase is never called */
final class FailedBindingPreventedValidationEvent(failure: BindingFailure, tag: ClassTag[?])
    extends FailedBindingEvent(failure, tag)
    with BindingAndValidationEvent

/** Event that is fired when binding phase succeeded in a BindingValidator, before the validation phase is called */
final class SuccessfulBindingBeforeValidationEvent(result: BindingPass[?], tag: ClassTag[?])
    extends SuccessfulBindingEvent(result, tag)

/** Event that is fired when validation phase failed in a BindingValidator */
final class SuccessfulBindingFailedValidationEvent(val result: Failure[?], val tag: ClassTag[?])
    extends BindingAndValidationEvent

/** Event that is fired when validation phase succeeded in a BindingValidator */
final class SuccessfulBindingAndValidationEvent(val result: BoundAndValid[?], val tag: ClassTag[?])
    extends BindingAndValidationEvent
