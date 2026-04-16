package com.magaran.svalidator.validation

import com.magaran.typedmap.TypedEntry

/** Base trait for all possible results of a validation process */
sealed trait ValidationResult:

  /** Returns a Some with the result of applying the partial function to this instance if it is defined for this
    * instance, otherwise returns None
    *
    * @param pf The partial function to apply
    * @tparam B The return type of the partial function
    */
  def collect[B](pf: PartialFunction[ValidationResult, B]): Option[B] = pf.lift(this)

/** Base trait for all possible results of a validation process that do not return additional success data */
sealed trait ResultWithoutData extends ValidationResult:

  def toEither: Either[Invalid, Unit] = this match
    case Valid      => Right(())
    case x: Invalid => Left(x)

  /** Invokes the target function if this instance is valid, otherwise returns this instance
    *
    * @param f Function to invoke
    */
  def mergeUsing[B >: Invalid <: ValidationResult](f: => B): B =
    this match
      case Valid      => f
      case x: Invalid => x

  /** Returns a valid result if this result and the argument are <code>Valid</code>, otherwise,
    * returns an <code>Invalid</code> with the [[ValidationFailure]]s of both
    */
  def merge(another: ResultWithoutData): ResultWithoutData =
    this match
      case Valid => another
      case x: Invalid =>
        another match
          case Valid      => x
          case y: Invalid => x.merge(y)

  /** Returns a Right if this result <code>Valid</code> and the argument is a <code>Right</code>, otherwise,
    * returns a <code>Left</code> with the [[ValidationFailure]]s of both
    */
  def merge[A](another: Either[Invalid, Unit]): Either[Invalid, Unit] =
    this match
      case Valid => another
      case x: Invalid =>
        another match
          case Right(_) => Left(x)
          case Left(y)  => Left(x.merge(y))

  /** Returns a valid result if this result and <code>another</code> are <code>Valid</code>, otherwise,
    * returns an <code>Invalid</code> with the [[ValidationFailure]]s of both.
    *
    * The additional data of the argument will be preserved if both results are valid
    */
  def merge[A](another: ResultWithSuccessData[A]): ResultWithSuccessData[A] =
    this match
      case Valid => another
      case x: Invalid =>
        another match
          case Valid(_)   => x
          case y: Invalid => x.merge(y)

  /** Converts this result into a result with additional success data attached.
    *
    * Note that if this result is invalid, the data will not be evaluated
    *
    * @param fun Lazy value that will be called to get success data, if this result is valid
    */
  def withData[B](fun: => B): ResultWithSuccessData[B] =
    this match
      case x: Invalid => x
      case Valid      => Valid(fun)

object ResultWithoutData {

  given Conversion[ResultWithoutData, Either[Invalid, Unit]] = _.toEither

}

sealed trait ResultWithSuccessData[+A] extends ValidationResult:

  /** Invokes the target function with the data of this instance if it is valid, otherwise returns this instance
    *
    * @param f Function to invoke
    */
  def mergeUsing[B >: Invalid <: ValidationResult](f: A => B): B =
    this match
      case Valid(a)   => f(a)
      case x: Invalid => x

  /** Returns a valid result if this result and <code>another</code> are <code>Valid</code>, otherwise,
    * returns an <code>Invalid</code> with the [[ValidationFailure]]s of both.
    *
    * The additional data of this instance will be preserved if both results are valid
    */
  def mergeWith(another: ResultWithoutData): ResultWithSuccessData[A] =
    another match
      case Valid => this
      case y: Invalid =>
        this match
          case Valid(_)   => y
          case x: Invalid => x.merge(y)

  /** Returns a valid result if this result and <code>another</code> are <code>Valid</code>, otherwise,
    * returns an <code>Invalid</code> with the [[ValidationFailure]]s of both.
    *
    * The additional data of both instances will be preserved if both results are valid, and will create a result whose
    * data is the tuple of the data from both instances
    */
  def mergeWith[B](another: ResultWithSuccessData[B]): ResultWithSuccessData[(A, B)] =
    this match
      case Valid(a) =>
        another match
          case Valid(b)   => Valid((a, b))
          case x: Invalid => x
      case x: Invalid =>
        another match
          case Valid(_)   => x
          case y: Invalid => x.merge(y)

  /** Strips the success data from this result if its <code>Valid</code>, or leave it as is if its <code>Invalid</code> */
  def withoutData: ResultWithoutData =
    this match
      case Valid(_)   => Valid
      case y: Invalid => y

object ResultWithSuccessData {

  given [A]: Conversion[ResultWithSuccessData[A], Either[Invalid, A]] = {
    case Valid(data)      => Right(data)
    case invalid: Invalid => Left(invalid)
  }

}

/** Represents a successful validation result that does not return additional success data */
object Valid extends ResultWithoutData

/** Represents a successful validation result that returns additional success data
  *
  * @param data Data returned by the validation process
  */
final case class Valid[+A](data: A) extends ResultWithSuccessData[A]

/** Represents a failed validation result.  This type is a bottom type for the type hierarchy of validation results
  *
  * @param validationFailures List of validation failures that occurred during the validation process
  */
final case class Invalid(validationFailures: ::[ValidationFailure])
    extends ResultWithoutData
    with ResultWithSuccessData[Nothing]:

  private var _validationMetadata: ValidationMetadata = ValidationMetadata.empty

  def localize(using Localizer): Invalid = {
    validationFailures match {
      case ::(head, tail) => Invalid(::(head.localize, tail.map(_.localize)))
    }
  }

  /** Returns the metadata associated with this instance */
  def validationMetadata: ValidationMetadata = _validationMetadata

  /** Returns a new instance of <code>Invalid</code> with the given metadata and the same failures as this instance */
  def withMetadata(metadata: ValidationMetadata): Invalid =
    val next = Invalid(validationFailures)
    next._validationMetadata = metadata
    next

  def withMetadata(entries: TypedEntry[?]*): Invalid =
    val next = Invalid(validationFailures)
    next._validationMetadata = ValidationMetadata(entries*)
    next

  /** Returns another instance of invalid merging the validation failures of both instances */
  def merge(another: Invalid): Invalid =
    Invalid(mergeNonEmptyLists(validationFailures, another.validationFailures))

  private def mergeNonEmptyLists(a: ::[ValidationFailure], b: ::[ValidationFailure]): ::[ValidationFailure] =
    a match
      case head :: Nil                      => ::(head, b)
      case ::(head, ::(nextHead, nextTail)) => ::(head, mergeNonEmptyLists(::(nextHead, nextTail), b))
