package testUtils

import com.magaran.svalidator.NestedFieldKey
import org.mockito.stubbing.OngoingStubbing
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.funspec.PathAnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class Observes extends PathAnyFunSpec with MockitoSugar with Matchers with FunctionStubbingHelpers:

  def when[T](method_call: T): OngoingStubbing[T] =
    Mockito.when(method_call)

  def reset[T](mock: T): T =
    Mockito.reset(mock)
    mock

  def nonEmptyList[A](a: A, as: A*): ::[A] = ::(a, as.toList)

extension [A](aMockObject: A)

  def wasToldTo(methodCall: A => Unit): Unit =
    methodCall(verify(aMockObject, atLeastOnce()))

  def wasNeverToldTo(methodCall: A => Unit): Unit =
    methodCall(verify(aMockObject, never()))

extension [A](aString: String)

  def asKey: NestedFieldKey = NestedFieldKey(aString)

  def quoted: String = StringBuilder().append('"').append(aString).append('"').mkString
