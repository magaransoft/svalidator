package testUtils

import org.scalatest.exceptions.TestFailedException

/** Group of stubbing helpers to make up for mockito lacking function stubs until ScalaMock can be mixed into
  * the ScalaTest FunSpec trait.
  */
trait FunctionStubbingHelpers:

  def stubFunction[A, R](expectedParameter: A, resultOnSuccess: R): A => R =
    val stub: (A => R) = arg =>
      if arg == expectedParameter then resultOnSuccess
      else
        throw TestFailedException(
          s"Function was called with wrong parameter: expected $expectedParameter, got $arg instead",
          1
        )
    stub

  def stubUnCallableFunction[A, R]: A => R =
    val stub: (A => R) = _ =>
      throw TestFailedException("Function was not expected to be called, but was called anyway", 1)
    stub

  def stubFunction[A, B, R](firstExpectedParameter: A, secondExpectedParameter: B, resultOnSuccess: R): (A, B) => R =
    val stub: (A, B) => R = (arg1, arg2) =>
      if arg1 == firstExpectedParameter && arg2 == secondExpectedParameter then resultOnSuccess
      else
        throw TestFailedException(
          "Function was called with wrong parameters, " +
            s"expected ($firstExpectedParameter,$secondExpectedParameter), got ($arg1,$arg2) instead.",
          1
        )
    stub

  def stubUnCallableFunction[A, B, R]: (A, B) => R =
    val stub: (A, B) => R = (_, _) =>
      throw TestFailedException("Function was not expected to be called, but was called anyway", 1)
    stub
