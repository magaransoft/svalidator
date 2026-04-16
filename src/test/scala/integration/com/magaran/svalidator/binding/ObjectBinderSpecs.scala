package integration.com.magaran.svalidator.binding

import java.time.LocalDate
import java.util.UUID

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.special.ObjectBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.FieldKey
import io.circe.Json
import testUtils.asKey
import testUtils.Observes

class ObjectBinderSpecs extends Observes:

  val sut: ObjectBinder = new ObjectBinder

  private given config: BindingConfig = BindingConfig.default

  private given context: BindingContext = BindingContext.empty

  private given localizer: Localizer = Localizer.NoLocalization

  describe("when binding a complex class with many types in the constructor"):

    val uuid = UUID.randomUUID()
    val fullClass = AComplexClass(
      aString = "someValue",
      anInt = 5,
      aLong = 8,
      aBoolean = true,
      aLocalDate = LocalDate.parse("2008-09-05", config.dateFormatter),
      optionalText = Some("someText"),
      optionalInt = Some(9),
      intList = List(10, 20, 30),
      longArray = Array(100L, 200L, 300L),
      setOfStrings = Set("a", "b", "c"),
      vectorOfFloats = Vector(1.0f, 2.0f, 3.0f),
      aTypeBasedEnum = AScala3Enum.ScalaBasedSecondOption,
      anUUID = uuid,
      aClassWithAGenericField = AClassWithAGenericField(3L)
    )

    describe("and the ValuesMap method of binding is used"):

      val initialSource = ValuesMap(
        "aString".asKey                               -> List("someValue"),
        "anInt".asKey                                 -> List("5"),
        "aLong".asKey                                 -> List("8"),
        "aBoolean".asKey                              -> List("true"),
        "aLocalDate".asKey                            -> List("2008-09-05"),
        "optionalText".asKey                          -> List("someText"),
        "optionalInt".asKey                           -> List("9"),
        "intList".asKey                               -> List("10", "20", "30"),
        "longArray".asKey                             -> List("100", "200", "300"),
        "setOfStrings".asKey                          -> List("a", "b", "c"),
        "vectorOfFloats".asKey                        -> List("1.0", "2.0", "3.0"),
        "aTypeBasedEnum".asKey                        -> List("2"),
        "anUUID".asKey                                -> List(uuid.toString),
        "aClassWithAGenericField.aGenericField".asKey -> List("3")
      )

      describe("and all values are provided"):

        val result = sut.bind[AComplexClass](initialSource, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          // Testing values individually for granularity purposes on test failure
          val instance = result.asInstanceOf[BindingPass[AComplexClass]].value
          instance.aBoolean shouldEqual fullClass.aBoolean
          instance.aLong shouldEqual fullClass.aLong
          instance.anInt shouldEqual fullClass.anInt
          instance.aString shouldEqual fullClass.aString
          instance.aLocalDate shouldEqual fullClass.aLocalDate
          instance.aTypeBasedEnum shouldEqual fullClass.aTypeBasedEnum
          instance.intList shouldEqual fullClass.intList
          instance.longArray shouldEqual fullClass.longArray
          instance.setOfStrings shouldEqual fullClass.setOfStrings
          instance.vectorOfFloats shouldEqual fullClass.vectorOfFloats
          instance.optionalInt shouldEqual fullClass.optionalInt
          instance.optionalText shouldEqual fullClass.optionalText
          instance.aClassWithAGenericField shouldEqual fullClass.aClassWithAGenericField
          instance shouldEqual fullClass

      describe("and the required string is missing"):

        given source: Source = initialSource - "aString".asKey
        val result           = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "aString".asKey

      describe("and the required int is missing"):

        given source: Source = initialSource - "anInt".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "anInt".asKey

      describe("and the required long is missing"):

        given source: Source = initialSource - "aLong".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "aLong".asKey

      describe("and the required boolean is missing"):
        given source: Source = initialSource - "aBoolean".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it " +
            "via constructor and use false for the missing boolean"
        ):
          result shouldEqual BindingPass(fullClass.copy(aBoolean = false))

      describe("and the required local date is missing"):

        given source: Source = initialSource - "aLocalDate".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "aLocalDate".asKey

      describe("and the required type based enum is missing"):

        given source: Source = initialSource - "aTypeBasedEnum".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "aTypeBasedEnum".asKey

      describe("and the optional text is missing"):

        given source: Source = initialSource - "optionalText".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result shouldEqual BindingPass(fullClass.copy(optionalText = None))

      describe("and the optional int is missing"):

        given source: Source = initialSource - "optionalInt".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result shouldEqual BindingPass(fullClass.copy(optionalInt = None))

      describe("and the list of integers is missing"):

        given source: Source = initialSource - "intList".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result shouldEqual BindingPass(fullClass.copy(intList = List()))

      describe("and the array of longs is missing"):

        given source: Source = initialSource - "longArray".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result shouldEqual BindingPass(fullClass.copy(longArray = Array.empty[Long]))

      describe("and the set of strings is missing"):

        given source: Source = initialSource - "setOfStrings".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result shouldEqual BindingPass(fullClass.copy(setOfStrings = Set.empty[String]))

      describe("and the required typeBasedEnum is missing"):
        given source: Source = initialSource - "aTypeBasedEnum".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "aTypeBasedEnum".asKey

      describe("and the required UUID is missing"):
        given source: Source = initialSource - "anUUID".asKey

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey shouldEqual "anUUID".asKey

    describe("and the JsonCursor method of binding is used"):

      val initialSource = JsonCursor(
        Json
          .obj(
            "aString"                 -> Json.fromString("someValue"),
            "anInt"                   -> Json.fromInt(5),
            "aLong"                   -> Json.fromLong(8),
            "aBoolean"                -> Json.fromBoolean(true),
            "aLocalDate"              -> Json.fromString("2008-09-05"),
            "optionalText"            -> Json.fromString("someText"),
            "optionalInt"             -> Json.fromInt(9),
            "intList"                 -> Json.fromValues(List(10, 20, 30).map(Json.fromInt)),
            "longArray"               -> Json.fromValues(List(100L, 200L, 300L).map(Json.fromLong)),
            "setOfStrings"            -> Json.fromValues(List("a", "b", "c").map(Json.fromString)),
            "vectorOfFloats"          -> Json.fromValues(List(1.0f, 2.0f, 3.0f).flatMap(Json.fromFloat)),
            "aTypeBasedEnum"          -> Json.fromInt(2),
            "anUUID"                  -> Json.fromString(uuid.toString),
            "aClassWithAGenericField" -> Json.obj("aGenericField" -> Json.fromInt(3))
          )
          .toString()
      ).toOption.get

      describe("and all values are provided"):

        val result = sut.bind[AComplexClass](initialSource, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          // Testing values individually for granularity purposes on test failure
          val casted   = result.asInstanceOf[BindingPass[AComplexClass]]
          val instance = casted.value
          instance.aBoolean should equal(fullClass.aBoolean)
          instance.aLong should equal(fullClass.aLong)
          instance.anInt should equal(fullClass.anInt)
          instance.aString should equal(fullClass.aString)
          instance.aLocalDate should equal(fullClass.aLocalDate)
          instance.aTypeBasedEnum should equal(fullClass.aTypeBasedEnum)
          instance.intList should equal(fullClass.intList)
          instance.longArray shouldEqual fullClass.longArray
          instance.setOfStrings shouldEqual fullClass.setOfStrings
          instance.vectorOfFloats shouldEqual fullClass.vectorOfFloats
          instance.optionalInt should equal(fullClass.optionalInt)
          instance.optionalText should equal(fullClass.optionalText)
          instance.aClassWithAGenericField shouldEqual fullClass.aClassWithAGenericField
          instance should equal(fullClass)

      describe("and the required string is missing"):

        given source: Source = initialSource.downField("aString").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("aString".asKey)

      describe("and the required int is missing"):

        given source: Source = initialSource.downField("anInt").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("anInt".asKey)

      describe("and the required long is missing"):

        given source: Source = initialSource.downField("aLong").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("aLong".asKey)

      describe("and the required boolean is missing"):

        given source: Source = initialSource.downField("aBoolean").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("return a binding failure for the missing required boolean field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("aBoolean".asKey)

      describe("and the required local date is missing"):

        given source: Source = initialSource.downField("aLocalDate").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("aLocalDate".asKey)

      describe("and the required type based enum is missing"):

        given source: Source = initialSource.downField("aTypeBasedEnum").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("aTypeBasedEnum".asKey)

      describe("and the required UUID is missing"):

        given source: Source = initialSource.downField("anUUID").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it("should return a binding failure for the missing required field"):
          val casted = result.asInstanceOf[BindingFailure]
          casted.fieldErrors should have size 1
          casted.fieldErrors.head.fieldKey should equal("anUUID".asKey)

      describe("and the optional text is missing"):

        given source: Source = initialSource.downField("optionalText").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result should equal(BindingPass(fullClass.copy(optionalText = None)))

      describe("and the optional int is missing"):

        given source: Source = initialSource.downField("optionalInt").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result should equal(BindingPass(fullClass.copy(optionalInt = None)))

      describe("and the list of integers is missing"):

        given source: Source = initialSource.downField("intList").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result should equal(BindingPass(fullClass.copy(intList = List())))

      describe("and the array of longs is missing"):

        given source: Source = initialSource.downField("longArray").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result should equal(BindingPass(fullClass.copy(longArray = Array.empty[Long])))

      describe("and the set of strings is missing"):

        given source: Source = initialSource.downField("setOfStrings").delete

        val result = sut.bind[AComplexClass](source, FieldKey.Root)

        it(
          "should return a binding result with a class instantiated with all the values in the map bound to it via constructor"
        ):
          result should equal(BindingPass(fullClass.copy(setOfStrings = Set.empty[String])))

  describe("when binding a type with another custom type its constructor"):

    describe("and the ValuesMap source method of binding is used"):

      describe("and the recursive type is a class that has a list of a custom type and it is indexed"):

        describe("and the values are normalized"):

          given source: Source = ValuesMap(
            "anIndexedList[0].stringField".asKey -> List("alpha"),
            "anIndexedList[0].longField".asKey   -> List("3"),
            "anIndexedList[1].stringField".asKey -> List("beta"),
            "anIndexedList[1].longField".asKey   -> List("9"),
            "anIndexedList[2].stringField".asKey -> List("gamma"),
            "anIndexedList[2].longField".asKey   -> List("1"),
            "anIndexedList[3].stringField".asKey -> List("lambda"),
            "anIndexedList[3].longField".asKey   -> List("39")
          )

          val result = sut.bind[AClassWithAnIndexedList](source, FieldKey.Root)

          it("should have bound properly the list and the recursive values"):
            result shouldEqual BindingPass(
              AClassWithAnIndexedList(
                List(
                  AnIndexedListValue("alpha", 3),
                  AnIndexedListValue("beta", 9),
                  AnIndexedListValue("gamma", 1),
                  AnIndexedListValue("lambda", 39)
                )
              )
            )

        describe("and the values are not normalized"):

          given source: Source = ValuesMap(
            "anIndexedList[0][stringField]".asKey -> List("alpha"),
            "anIndexedList[0][longField]".asKey   -> List("3"),
            "anIndexedList[1][stringField]".asKey -> List("beta"),
            "anIndexedList[1][longField]".asKey   -> List("9"),
            "anIndexedList[2][stringField]".asKey -> List("gamma"),
            "anIndexedList[2][longField]".asKey   -> List("1"),
            "anIndexedList[3][stringField]".asKey -> List("lambda"),
            "anIndexedList[3][longField]".asKey   -> List("39")
          )

          val result = sut.bind[AClassWithAnIndexedList](source, FieldKey.Root)

          it("should have bound properly the list and the recursive values"):
            result shouldEqual BindingPass(
              AClassWithAnIndexedList(
                List(
                  AnIndexedListValue("alpha", 3),
                  AnIndexedListValue("beta", 9),
                  AnIndexedListValue("gamma", 1),
                  AnIndexedListValue("lambda", 39)
                )
              )
            )

    describe("and the JsonCursor method of binding is used"):

      describe("and the recursive type is a class that has a list of a custom type and it is indexed"):

        given source: Source =
          JsonCursor(
            Json
              .obj(
                "anIndexedList" ->
                  Json.fromValues(
                    List(
                      Json.obj("stringField" -> Json.fromString("alpha"), "longField"  -> Json.fromLong(3)),
                      Json.obj("stringField" -> Json.fromString("beta"), "longField"   -> Json.fromLong(9)),
                      Json.obj("stringField" -> Json.fromString("gamma"), "longField"  -> Json.fromLong(1)),
                      Json.obj("stringField" -> Json.fromString("lambda"), "longField" -> Json.fromLong(39))
                    )
                  )
              )
              .toString()
          ).toOption.get

        val result = sut.bind[AClassWithAnIndexedList](source, FieldKey.Root)

        it("should have bound properly the list and the recursive values"):
          result should equal(
            BindingPass(
              AClassWithAnIndexedList(
                List(
                  AnIndexedListValue("alpha", 3),
                  AnIndexedListValue("beta", 9),
                  AnIndexedListValue("gamma", 1),
                  AnIndexedListValue("lambda", 39)
                )
              )
            )
          )
