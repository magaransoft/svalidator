package com.magaran.svalidator.binding.binders.special

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.NestedFieldKey
import io.circe.Json
import testUtils.Observes

class SetBinderWrapperSpecs extends Observes:

  private given config: BindingConfig    = BindingConfig.default
  private given metadata: BindingContext = BindingContext.empty
  private val fieldName                  = "theSetFieldName"
  private val fieldKey                   = NestedFieldKey(fieldName)

  private val sut = summon[TypedBinder[Set[Long]]]

  describe("when binding a list of values using the values map source of binding"):

    describe("and the list is empty"):
      given source: Source = ValuesMap.empty

      val result = sut.bind(source, fieldKey)

      it("should bind the empty list properly"):
        result should equal(BindingPass(Set()))

    describe("and the list of values is of a specific type with a single non indexed field name"):

      describe("and some of the values return binding failures"):
        given source: Source = ValuesMap(fieldKey -> List("a", "2", "c", "4"))

        val result = sut.bind(source, fieldKey)

        it(
          "should have a list of binding failures for each failure encountered with the field name as returned by " +
            "the wrappedBinder"
        ):
          val resultFailure = result.asInstanceOf[BindingFailure]
          resultFailure.fieldErrors should have size 2
          resultFailure.fieldErrors.exists(_.fieldKey == fieldKey.indexed(0)) should be(true)
          resultFailure.fieldErrors.exists(_.fieldKey == fieldKey.indexed(2)) should be(true)

      describe("and none of the values return binding failures"):
        given source: Source = ValuesMap(fieldKey -> List("1", "2", "3", "4"))

        val result = sut.bind(source, fieldKey)

        it("should have returned BindingPass with a list with all BindingPass values bound to it"):
          result should equal(BindingPass(Set(1L, 2L, 3L, 4L)))

    describe("and the list of values is of a specific type with indexed field names"):

      describe("and some of them return binding failures"):

        given source: Source = ValuesMap(
          fieldKey.indexed(0) -> List("1"),
          fieldKey.indexed(1) -> List("b"),
          fieldKey.indexed(2) -> List("3"),
          fieldKey.indexed(3) -> List("d")
        )

        val result = sut.bind(source, fieldKey)

        it(
          "should have returned BindingFailure with a field error for all failing values and the field name " +
            "should be the same as returned by the wrapped binder"
        ):
          val resultFailure = result.asInstanceOf[BindingFailure]
          resultFailure.fieldErrors should have size 2
          resultFailure.fieldErrors.exists(_.fieldKey == fieldKey.indexed(1)) should be(true)
          resultFailure.fieldErrors.exists(_.fieldKey == fieldKey.indexed(3)) should be(true)

      describe("and none of them return binding failures"):
        given source: Source = ValuesMap(
          fieldKey.indexed(0) -> List("1"),
          fieldKey.indexed(1) -> List("2"),
          fieldKey.indexed(2) -> List("3"),
          fieldKey.indexed(3) -> List("4")
        )

        val result = sut.bind(source, fieldKey)

        it("should have returned BindingPass with a list with all BindingPass values bound to it"):
          result should equal(BindingPass(Set(1L, 2L, 3L, 4L)))

  describe("when binding a list of values using the JsonCursor source of binding"):

    describe("and the collection is not present"):
      val json             = new JsonCursor(Json.obj().hcursor)
      given source: Source = json.downField(fieldName)

      val result = sut.bind(source, fieldKey)

      it("should have bound the collection properly"):
        result should equal(BindingPass(Set()))

    describe("and the collection is null"):
      val json             = JsonCursor(fieldKey -> Json.Null)
      given source: Source = json.downField(fieldName)

      val result = sut.bind(source, fieldKey)

      it("should have bound the collection properly"):
        result should equal(BindingPass(Set()))

    describe("and the collection is empty"):
      val json             = JsonCursor(fieldKey -> Json.fromValues(Set()))
      given source: Source = json.downField(fieldName)

      val result = sut.bind(source, fieldKey)

      it("should have bound the collection properly"):
        result should equal(BindingPass(Set()))

    describe("and some of the values return binding failures"):

      val json = JsonCursor(
        fieldKey -> Json
          .fromValues(Set(Json.fromString("a"), Json.fromLong(2), Json.fromString("c"), Json.fromLong(4)))
      )
      given source: Source = json.downField(fieldName)
      val result           = sut.bind(source, fieldKey)

      it(
        "should have a list of binding failures for each failure encountered, with the name of list field instead of the" +
          " name of sub-field returned by the wrappedBinder"
      ):
        val casted = result.asInstanceOf[BindingFailure]
        casted.fieldErrors should have size 2
        casted.fieldErrors.exists(_.fieldKey == fieldKey.indexed(0)) should be(true)
        casted.fieldErrors.exists(_.fieldKey == fieldKey.indexed(2)) should be(true)

    describe("and none of the values return binding failures"):

      val json = JsonCursor(
        fieldKey -> Json.fromValues(Set(Json.fromLong(1), Json.fromLong(2), Json.fromLong(3), Json.fromLong(4)))
      )

      given source: Source = json.downField(fieldName)

      val result = sut.bind(source, fieldKey)

      it("should have returned BindingPass with a list with all BindingPass values bound to it"):
        result should equal(BindingPass(Set(1L, 2L, 3L, 4L)))
