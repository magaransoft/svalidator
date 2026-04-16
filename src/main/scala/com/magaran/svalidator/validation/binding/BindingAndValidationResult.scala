package com.magaran.svalidator.validation.binding

import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingResult
import com.magaran.svalidator.binding.Source
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.ValidationFailure
import com.magaran.svalidator.validation.ValidationMetadata

/** Represents the result of binding and validation in tandem of a single instance of a type.
  *
  * @tparam A Type of the validated instance
  */
sealed trait BindingAndValidationResult[+A]:

  /** Returns the [[com.magaran.svalidator.binding.BindingResult]] that was generated when
    * binding was performed.  Note that the instance type may not be the same as the instance type of
    * the validation result since map operations can be performed on the binding result before validation.
    */
  def bindingResult: BindingResult[?]

  /** Returns the [[com.magaran.svalidator.binding.Source]] of values that was provided when binding
    * was performed
    */
  def source: Source = bindingResult.source

  /** Returns the [[com.magaran.svalidator.binding.BindingContext]] that was provided when binding
    * was performed
    */
  def context: BindingContext = bindingResult.context

  /** If this instance represents a [[Failure]], returns a new <code>Failure</code> with all
    * [[ValidationFailure]] messages localized using the given localizer. Otherwise, return this
    * instance unchanged.
    */
  def localize(using localizer: Localizer): BindingAndValidationResult[A]

  /** Returns a Some with the result of applying the partial function to this instance if it is defined for this
    * instance, otherwise returns None
    *
    * @param pf The partial function to apply
    * @tparam B The return type of the partial function
    */
  def collect[B](pf: PartialFunction[BindingAndValidationResult[A], B]): Option[B] = pf.lift(this)

/** Parent trait for binding and validation results that can contain additional success data */
sealed trait BindingAndValidationResultWithSuccessData[+A, +B] extends BindingAndValidationResult[A]:

  def withoutData: BindingAndValidationResultWithoutData[A] = ??? /* this match {
    case x: SuccessWithData[A, B] => Success(x.instance)(x.bindingResult)
    case x: Failure[?]            => Failure(x.failures)(x.bindingResult)(x.validationMetadata)
  }*/

/** Parent trait for binding and validation results that do not contain additional success data */
sealed trait BindingAndValidationResultWithoutData[+A] extends BindingAndValidationResult[A]

/** Parent class for binding and validation results that were bound and validated successfully, regardless
  * of the presence of additional data
  */
sealed abstract class BoundAndValid[+A](val bindingResult: BindingResult[?]) extends BindingAndValidationResult[A]:

  /** Returns the instance that was validated successfully */
  def instance: A

/** Represents summaries that were bound and validated successfully, and have no additional success data.
  *
  * @param instance Instance that was validated during the validation phase
  * @tparam A Type of the validated instance
  */
final case class Success[+A](instance: A)(bindingResult: BindingResult[?])
    extends BoundAndValid[A](bindingResult)
    with BindingAndValidationResultWithoutData[A]():
  override def localize(using Localizer): Success[A] = this

/** Represents summaries that were bound and validated successfully, and contain additional success data
  *
  * @param instance Instance that was validated during the validation phase
  * @param data Additional data that was generated during the validation phase
  * @tparam A Type of the validated instance
  */
final case class SuccessWithData[+A, B](instance: A, data: B)(bindingResult: BindingResult[?])
    extends BoundAndValid[A](bindingResult)
    with BindingAndValidationResultWithSuccessData[A, B]():
  override def localize(using Localizer): SuccessWithData[A, B] = this

/** Represents summaries failed either binding or validation.
  *
  * @param failures Errors that occurred during binding or validation of the instance
  * @tparam A Type of the validated instance
  */
final case class Failure[+A](failures: List[ValidationFailure])(val bindingResult: BindingResult[?])(
  using val validationMetadata: ValidationMetadata
) extends BindingAndValidationResultWithoutData[A]
    with BindingAndValidationResultWithSuccessData[A, Nothing]:

  /** Generates a new summary by applying the given localizer to the
    * [[com.magaran.svalidator.validation.ValidationFailure#localize ValidationFailure.localize]]'s method of all failures
    * contained.
    *
    * @param localizer Localizer to apply to failures
    */
  def localize(using Localizer): Failure[Nothing] =
    Failure(failures.map(_.localize))(bindingResult)
