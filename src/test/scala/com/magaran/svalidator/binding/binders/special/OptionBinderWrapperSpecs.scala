package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.typed.LongBinder
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.Observes

class OptionBinderWrapperSpecs extends Observes:

  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val fieldName                  = "fieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)

  private val wrappedBinder                  = LongBinder()
  private val sut: TypedBinder[Option[Long]] = OptionBinder(using wrappedBinder)

  describe("when performing the binding of an option type"):

    describe("and a values map source of binding is used"):

      describe("and the wrapped type binder returns a BindingFailure"):

        describe("and the field is missing in the values map"):

          given source: Source = ValuesMap(NestedFieldKey(fieldName + "somethingElse") -> List("a"))

          val result = sut.bind(source, fieldKey)

          it("should return a Binding Pass with a value of None"):
            result should equal(BindingPass(None))

        describe("and field can not be bound by the wrapped mapper"):

          given source: Source = ValuesMap(fieldKey -> List("a"))

          val result = sut.bind(source, fieldKey)

          it("should return a Binding failure with the error and exception provided"):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.size should equal(1)
            casted.cause.getClass shouldNot equal(classOf[NoSuchElementException])
            val error = casted.fieldErrors.head
            error.fieldKey should equal(fieldKey)

      describe("and the wrapped binder returns a binding pass"):

        given source: Source = ValuesMap(fieldKey -> List("8"))

        val result = sut.bind(source, fieldKey)

        it("should return a BindingPass with the valueGetter returned from the wrapped binder wrapped in Option"):
          result should equal(BindingPass(Option(8L)))

    describe("and a JsonCursor source of binding is used"):

      describe("and the wrapped type binder returns a BindingFailure"):

        describe("and the binding failure was caused by a no such element exception due to absence of value"):

          given source: Source = new JsonCursor(Json.obj().hcursor).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it("should return a Binding Pass with a value of None"):
            result should equal(BindingPass(None))

        describe("and the binding failure was caused by a no such element exception from presence of a null value"):

          given source: Source = JsonCursor(fieldKey -> Json.Null).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it("should return a Binding Pass with a value of None"):
            result should equal(BindingPass(None))

        describe("and the binding failure was not caused by a no such element exception"):

          val source = JsonCursor(fieldKey -> Json.fromString("a")).downField(fieldName)

          val result = sut.bind(source, fieldKey)

          it("should return a Binding failure with the error and exception provided"):
            val casted = result.asInstanceOf[BindingFailure]
            casted.fieldErrors.size should equal(1)
            casted.cause.getClass shouldNot equal(classOf[NoSuchElementException])

      describe("and the wrapped type binder returns a BindingPass"):

        val fieldValue       = 8L
        given source: Source = JsonCursor(fieldKey -> Json.fromLong(fieldValue)).downField(fieldName)

        val result = sut.bind(source, fieldKey)

        it("should return a BindingPass with the valueGetter returned from the wrapped binder wrapped in Option"):
          result should equal(BindingPass(Option(fieldValue)))
