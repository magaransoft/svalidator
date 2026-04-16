package integration.com.magaran.svalidator.validation.simple

import scala.language.implicitConversions

import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.binding.ValuesMap
import com.magaran.svalidator.testing.extensions.validation.withoutdata.shouldBeValid
import com.magaran.svalidator.testing.extensions.validation.withoutdata.shouldHaveValidationErrorFor
import com.magaran.svalidator.testing.extensions.validation.withoutdata.shouldNotHaveValidationErrorFor
import com.magaran.svalidator.validation
import com.magaran.svalidator.validation.*
import com.magaran.svalidator.validation.binding.*
import com.magaran.svalidator.validation.functional.FunctionalValidator
import com.magaran.svalidator.validation.simple.SimpleValidator
import com.magaran.typedmap.TypedKey
import testUtils.asKey
import testUtils.Observes

case class OptionalRange(min: Option[Int], max: Option[Int])

case class Address(line1: String, line2: String, city: String, state: String, zip: String)

case class PhoneNumber(areaCode: String, number: String)

case class MultipleValuesWithinRange(firstNumber: Option[Int], secondNumber: Option[Int], thirdNumber: Option[Int])

enum Gender:
  case Male
  case Female

case class Person(
  firstName: String,
  lastName: String,
  age: Int,
  gender: Gender,
  married: Boolean,
  hasJob: Boolean,
  notes: Option[String],
  primaryAddress: Address,
  otherAddresses: List[Address],
  tasksCompletedByMonth: Vector[Int],
  emergencyPhoneNumber: Option[PhoneNumber]
)

class OptionalRangeValidator extends SimpleValidator[OptionalRange]:

  def validate(using OptionalRange): ResultWithoutData =
    WithRules(
      For { _.min }
        must { _.isDefined } withMessage "Must be defined"
        andRuleFor { _.max } ForField "max"
        must { _.isDefined } withMessage "Must be defined"
        andRuleFor { x => (x.min.get, x.max.get) } ForField "range"
        must { (min, max) => min <= max } withMessage "Min must be less than or equal to max"
    )

val a = new OptionalRangeValidator

class OptionalRangeValidatorAlt extends SimpleValidator[OptionalRange]:

  def validate(using OptionalRange): ResultWithoutData =
    WithRules(
      RuleFor { identity } ForField "min"
        must { _.min.isDefined } withMessage "Min must be defined"
        switchFieldTo "max"
        must { _.max.isDefined } withMessage "Max must be defined"
        switchFieldTo "range"
        must { x => x.min.get <= x.max.get } withMessage "Min must be less than or equal to max"
    )

class OptionalRangeValidatorAlt2 extends SimpleValidator[OptionalRange]:

  private val emptyOptionBoolean: Option[Boolean] = None

  override def validate(using instance: OptionalRange): ResultWithoutData =
    WithRules(
      For { _.min }
        must { _.isDefined } withMessage "Must be defined",
      For { _.max }
        must { _.isDefined } withMessage "Must be defined",
      When(x => x.min.isDefined && x.max.isDefined)(
        RuleFor { x => (x.min.get, x.max.get) } ForField "range"
          must { (min, max) => min <= max }
          withMessage "Min(%d) must be less than or equal to max(%d)" withFormat (instance.min.get, instance.max.get)
      ),
      // Testing laziness of must, when and withFormat
      When(_ => false)(
        For { _.min }
          must emptyOptionBoolean.get
          withMessage "Testing laziness of must receiving => Boolean" withFormat instance.min.get
          must { _ => false } when instance.min.get > 100
          withMessage "Testing laziness of when receiving => Boolean"
      )
    )

class OptionalRangeValidatorAlt3 extends SimpleValidator[OptionalRange]:

  override def validate(using instance: OptionalRange): ResultWithoutData =
    WithRules(
      For { _.min }
        must { _.isDefined } withMessage "Must be defined",
      For { _.max }
        must { _.isDefined } withMessage "Must be defined",
      When(x => x.min.isDefined && x.max.isDefined)(
        RuleFor { x => (x.min.get, x.max.get) } ForField "range"
          must { (min, max) => min <= max }
          withMessage "The maximum(%d) value is greater than the minimum you specified" withFormat instance.max.get
      )
    )

class AddressValidator extends SimpleValidator[Address]:

  // Parenthesized For should also work, keep this one as a test
  def validate(using Address): ResultWithoutData = WithRules(
    For(_.zip)
      must { _.length <= 10 } withMessage "Must have %d characters or less" withFormat 10
  )

class PhoneNumberValidator extends SimpleValidator[PhoneNumber]:
  def validate(using PhoneNumber): ResultWithoutData = WithRules(
    For(x => x.areaCode)
      must { _.length <= 4 } withMessage "The area code can not exceed 4 characters"
  )

class MultipleFieldValidator extends SimpleValidator[MultipleValuesWithinRange] {

  // Test all three shapes for extracting fields
  private val firstNumbField    = Field { x => x.firstNumber }
  private val secondNumberField = Field { _.secondNumber }
  private val thirdNumberField  = Field(_.thirdNumber)

  // Single test to check Key function works fine
  private val firstNumberKey = Key(_.firstNumber)

  def validate(using MultipleValuesWithinRange): ResultWithoutData = {
    WithRules(
      RuleForMultiple(firstNumbField, secondNumberField, thirdNumberField)
        must { _ => firstNumberKey == firstNumbField.fieldKey } withMessage "Field keys should match"
        must { _.isDefined } withMessage "Can not be empty"
        map { _.get }
        must { _ > 0 } withMessage "Must be a positive number"
        must { _ < 100 } withMessage "Must be less than 100",
    )
  }
}

class MultipleFieldValidatorAlt extends SimpleValidator[MultipleValuesWithinRange] {

  def validate(using MultipleValuesWithinRange): ResultWithoutData = {
    WithRules(
      ForMultiple(_.firstNumber, _.secondNumber, _.thirdNumber)
        must { _.isDefined } withMessage "Can not be empty"
        map { _.get }
        must { _ > 0 } withMessage "Must be a positive number"
        must { _ < 100 } withMessage "Must be less than 100",
    )
  }
}

object ErrorCodeTypedKey extends TypedKey[String]:
  override def toString: String = "ERROR-CODE"

class PersonValidator extends SimpleValidator[Person]:

  override def validate(using instance: Person): ResultWithoutData =
    WithRules(
      For { x => x.firstName }
        mustNot { _.isEmpty } withMessage "First name is required"
        must { _.length <= 32 } withMessage "Must have 32 characters or less",
      For { _.lastName }
        mustNot { _.isEmpty } withMessage "Last name is required"
        must { _.length <= 32 } withMessage "Must have 32 characters or less" withMetadata ErrorCodeTypedKey -> "1504",
      For { _.age }
        must { _ >= 0 } withMessage "Must be a non negative number",
      When { _.age < 21 }(
        For { _.hasJob }
          must { _ == false } withMessage "Must be 21 years or older to allow marking a job",
        For { _.married }
          must { _ == false }
          withMessage "Can't be married at %d Must be 21 years or older to allow marking marriage"
          withFormat instance.age
      ),
      // Compile time test for nested field property name extractions
      For { _.primaryAddress.line1 }
        must { _.length >= 5 } withMessage "Line 1 must have at least 5 characters",
      For { _.tasksCompletedByMonth }
        must { _.size == 12 } withMessage "Must have 12 values for the tasks completed by month",
      For { _.notes }
        must { _.isDefined } withMessage "Notes are required just cause we want to test map"
        map { _.get }
        must { _.length <= 32 } withMessage "Notes can't have more than 32 characters"
        map { _.charAt(0) }
        must { _.isLetter } withMessage "Must start with a letter",
      ForEach { x => x.tasksCompletedByMonth }
        must { _ > 0 } withMessage "Must be a positive number",
      ForComponent { _.primaryAddress }
        validateUsing AddressValidator(),
      For { _.emergencyPhoneNumber }
        mustNot { _.isEmpty } when { instance.age < 21 }
        withMessage "An emergency number is needed if the person is a minor",
      ForOptionalComponent { _.emergencyPhoneNumber }
        validateUsing PhoneNumberValidator(),
      ForEachComponent { _.otherAddresses }
        validateUsing AddressValidator(),
    )

/** We repeat the same validator in the functional style */
class FunctionalPersonValidator extends FunctionalValidator[Person, Unit, Unit, Unit]:

  override def validate(instance: Person, inputData: Unit)(using Unit): Either[Invalid, Unit] = {
    WhenValidating(instance)
      .withSuccessData(())
      .withRules(
        For { x => x.firstName }
          mustNot { _.isEmpty } withMessage "First name is required"
          must { _.length <= 32 } withMessage "Must have 32 characters or less",
        For { _.lastName }
          mustNot { _.isEmpty } withMessage "Last name is required"
          must {
            _.length <= 32
          } withMessage "Must have 32 characters or less" withMetadata ErrorCodeTypedKey -> "1504",
        For { _.age }
          must { _ >= 0 } withMessage "Must be a non negative number",
        When { _.age < 21 }(
          For { _.hasJob }
            must { _ == false } withMessage "Must be 21 years or older to allow marking a job",
          For { _.married }
            must { _ == false }
            withMessage "Can't be married at %d Must be 21 years or older to allow marking marriage"
            withFormat instance.age
        ),
        // Compile time test for nested field property name extractions
        For { _.primaryAddress.line1 }
          must { _.length >= 5 } withMessage "Line 1 must have at least 5 characters",
        For { _.tasksCompletedByMonth }
          must { _.size == 12 } withMessage "Must have 12 values for the tasks completed by month",
        For { _.notes }
          must { _.isDefined } withMessage "Notes are required just cause we want to test map"
          map { _.get }
          must { _.length <= 32 } withMessage "Notes can't have more than 32 characters"
          map { _.charAt(0) }
          must { _.isLetter } withMessage "Must start with a letter",
        ForEach { x => x.tasksCompletedByMonth }
          must { _ > 0 } withMessage "Must be a positive number",
        ForComponent { _.primaryAddress }
          validateUsing AddressValidator(),
        For { _.emergencyPhoneNumber }
          mustNot { _.isEmpty } when { instance.age < 21 }
          withMessage "An emergency number is needed if the person is a minor",
        ForOptionalComponent { _.emergencyPhoneNumber }
          validateUsing PhoneNumberValidator(),
        ForEachComponent { _.otherAddresses }
          validateUsing AddressValidator(),
      )
  }

class SimpleValidatorIntegrationSpecs extends Observes:

  given context: BindingContext = BindingContext.empty

  val sut: Validator[Person, ResultWithoutData] = PersonValidator()

  /* We wrap around the functional variant in a regular validator to test it in the same way as the simple one.*/
  val functionalSut: Validator[Person, ResultWithoutData] = instance ?=> {
    val context: Unit = ()
    FunctionalPersonValidator().validate(instance, ())(using context) match {
      case Left(invalidResult) => invalidResult
      case Right(_)            => Valid
    }
  }

  Vector(sut, functionalSut).foreach: sut =>
    val targetSutClass = if sut.isInstanceOf[PersonValidator] then "PersonValidator" else "FunctionalPersonValidator"

    describe(s"when validating an instance of a person using the $targetSutClass"):

      val instance = Person(
        firstName = "John",
        lastName = "Smith",
        age = 25,
        gender = Gender.Male,
        married = true,
        hasJob = true,
        notes = Some("notes"),
        primaryAddress = Address("line1", "line2", "city", "state", "someZip"),
        otherAddresses = List(
          Address("line1", "line2", "city", "state", "someZip"),
          Address("anotherLine1", "anotherLine2", "anotherCity", "anotherState", "anotherZip")
        ),
        tasksCompletedByMonth = Vector(7, 4, 9, 3, 10, 6, 15, 59, 4, 2, 1, 2),
        emergencyPhoneNumber = Some(PhoneNumber(areaCode = "123", number = "4567890"))
      )

      describe("and all fields are filled properly"):

        val result = sut.validate(using instance)

        it("should be valid"):
          result.shouldBeValid()

      describe("and the first name is empty"):

        val result = sut.validate(using instance.copy(firstName = ""))

        it("should have a validation error for the firstName field"):
          result shouldHaveValidationErrorFor "firstName"

      describe("and the first name has more than 32 characters"):

        val result = sut.validate(using instance.copy(firstName = "012345678901234567890123456789012"))

        it("should have a validation error for the firstName field"):
          result shouldHaveValidationErrorFor "firstName"

      describe("and the last name is empty"):

        val result = sut.validate(using instance.copy(lastName = ""))

        it("should have a validation error for the lastName field"):
          result shouldHaveValidationErrorFor "lastName"

      describe("and the last name has more than 32 characters"):

        val result = sut.validate(using instance.copy(lastName = "012345678901234567890123456789012"))

        it("should have a validation error for the lastName field"):
          result shouldHaveValidationErrorFor "lastName"

        it("should have metadata added for the error of this field"):
          result
            .asInstanceOf[validation.Invalid]
            .validationFailures
            .filter(_.fieldKey == "lastName".asKey)
            .head
            .metadata(ErrorCodeTypedKey) shouldEqual "1504"

      describe("and the age is negative"):

        val result = sut.validate(using instance.copy(age = -1))

        it("should have a validation error for the age field"):
          result shouldHaveValidationErrorFor "age"

      describe("and the notes are not present"):

        val result = sut.validate(using instance.copy(notes = None))

        it("should have a validation error for the notes field"):
          result shouldHaveValidationErrorFor "notes"

      describe("and the notes are defined but have more than 32 characters"):
        val result = sut.validate(
          using instance
            .copy(notes = Some("A ridiculously long string that should have more than 32 characters by all means"))
        )

        it("should have a validation error for the notes field"):
          result shouldHaveValidationErrorFor "notes"

      describe("and the notes are defined and have less than 32 characters but don't start with a letter"):
        val result = sut.validate(using instance.copy(notes = Some("3 This starts with a number")))

        it("should have a validation error for the notes field"):
          result shouldHaveValidationErrorFor "notes"

      describe("and the married flag is set to true but the age is lower than the marriageable age of 21"):

        val result = sut.validate(using instance.copy(age = 20, married = true))

        it("should have a validation error for the married field"):
          result shouldHaveValidationErrorFor "married"

      describe("and the primary address line1 is shorter than 5 characters"):

        val result = sut.validate(using instance.copy(primaryAddress = instance.primaryAddress.copy(line1 = "1234")))

        it("should have a validation error for the married field"):
          result shouldHaveValidationErrorFor "primaryAddress.line1"

      describe("and the hasJob flag is set to true but the age is lower than the minimum working age of 21"):

        val result = sut.validate(using instance.copy(age = 20, hasJob = true))

        it("should have a validation error for the married field"):
          result shouldHaveValidationErrorFor "hasJob"

      describe("and the primary address component zip is longer than 10 characters"):

        val result =
          sut.validate(
            using instance.copy(primaryAddress = instance.primaryAddress.copy(zip = "ARidiculouslyLongZipCodeHere"))
          )

        it("should have a validation error for the primary address zip"):
          result shouldHaveValidationErrorFor "primaryAddress.zip"

      describe("and one of the addresses zip is longer than 10 characters"):

        val result = sut.validate(
          using instance.copy(otherAddresses =
            List(
              Address("line1", "line2", "city", "state", "someZip"),
              Address("anotherLine1", "anotherLine2", "anotherCity", "anotherState", "aVeryLongZipCodeHere")
            )
          )
        )

        it(
          "should have a validation error for the addresses field on the index of the invalid address followed by a dot and the name of the invalid field"
        ):
          result shouldHaveValidationErrorFor "otherAddresses[1].zip"

      describe("and both of the addresses zip are longer than 10 characters"):

        val result = sut.validate(
          using instance.copy(otherAddresses =
            List(
              Address("line1", "line2", "city", "state", "aVeryLongZipCodeHere"),
              Address("anotherLine1", "anotherLine2", "anotherCity", "anotherState", "anotherVeryLongZipCodeHere")
            )
          )
        )

        it(
          "should have a validation error for each the addresses field on the index of the invalid address followed by a dot and the name of the invalid field"
        ):
          result shouldHaveValidationErrorFor "otherAddresses[0].zip"
          result shouldHaveValidationErrorFor "otherAddresses[1].zip"

      describe("and the size of number of tasks completed by month is bigger than 12"):

        val result =
          sut.validate(using instance.copy(tasksCompletedByMonth = Vector(7, 4, 9, 3, 10, 6, 15, 59, 4, 2, 1, 2, 8)))

        it("should have a validation error for the tasks completed by month"):
          result shouldHaveValidationErrorFor "tasksCompletedByMonth"

      describe("and the size of number of tasks completed by month is less than 12"):

        val result =
          sut.validate(using instance.copy(tasksCompletedByMonth = Vector(7, 4, 9, 3, 10, 6, 15, 59, 4, 2, 1)))

        it("should have a validation error for the tasks completed by month"):
          result shouldHaveValidationErrorFor "tasksCompletedByMonth"

      describe("and one of the number of tasks is less than zero"):

        val result =
          sut.validate(using instance.copy(tasksCompletedByMonth = Vector(7, 4, -9, 3, -10, 6, 15, 59, 4, -2, 1, 19)))

        it("should have a validation error for the tasks completed by month in the invalid indexes"):
          result shouldHaveValidationErrorFor "tasksCompletedByMonth[2]"
          result shouldHaveValidationErrorFor "tasksCompletedByMonth[4]"
          result shouldHaveValidationErrorFor "tasksCompletedByMonth[9]"

      describe("and the emergency phone number is not provided"):

        describe("and the person is 21 or older"):
          val result = sut.validate(using instance.copy(emergencyPhoneNumber = None, age = 21))

          it("should not have a validation error for the emergencyPhoneNumber field"):
            result shouldNotHaveValidationErrorFor "emergencyPhoneNumber"

        describe("and the person is younger than 21"):
          val result = sut.validate(using instance.copy(emergencyPhoneNumber = None, age = 20))

          it("should have a validation error for the emergencyPhoneNumber field"):
            result shouldHaveValidationErrorFor "emergencyPhoneNumber"

      describe("and the emergency phone number is provided and the area code has more than four digits"):

        val result =
          sut.validate(
            using instance.copy(emergencyPhoneNumber = Some(PhoneNumber(areaCode = "99990", number = "aNumber")))
          )

        it("should have a validation error for the emergencyPhoneNumber.areaCode field"):
          result shouldHaveValidationErrorFor "emergencyPhoneNumber.areaCode"

  describe("when validating an instance of an optional range using various validators"):
    val sut1: Validator[OptionalRange, ResultWithoutData] = OptionalRangeValidator()
    val sut2: Validator[OptionalRange, ResultWithoutData] = OptionalRangeValidatorAlt()
    val sut3: Validator[OptionalRange, ResultWithoutData] = OptionalRangeValidatorAlt2()
    val sut4: Validator[OptionalRange, ResultWithoutData] = OptionalRangeValidatorAlt3()

    List(sut1, sut2, sut3, sut4).foreach: sut =>
      val instance = OptionalRange(Some(1), Some(10))

      describe(s"when validating an instance of an optional range using ${sut.getClass.getSimpleName}"):
        describe("both min and max are defined"):

          describe("and they are in a valid range"):
            val result = sut.validate(using instance)
            it("should not have a validation error for the range field"):
              result.shouldBeValid()

          describe("and the min is greater than the max"):
            val result = sut.validate(using instance.copy(min = instance.max.map(_ + 1)))
            it("should have a validation error for the range field"):
              result shouldHaveValidationErrorFor "range"

        describe("and neither min nor max are defined"):
          val result = sut.validate(using instance.copy(min = None, max = None))
          it("should have a validation error for the min field"):
            result shouldHaveValidationErrorFor "min"

        describe("and the max is defined but the min is not"):
          val result = sut.validate(using instance.copy(min = None))
          it("should not have a validation error for the min field"):
            result shouldHaveValidationErrorFor "min"

        describe("and the min is defined but the max is not"):
          val result = sut.validate(using instance.copy(max = None))
          it("should not have a validation error for the max field"):
            result shouldHaveValidationErrorFor "max"

  describe("when validating an instance of multiple values within range with various validators"):

    val sut1 = new MultipleFieldValidator()
    val sut2 = new MultipleFieldValidatorAlt()

    List(sut1, sut2).foreach: sut =>
      describe(s"when validating an instance of a multiple values within range using ${sut.getClass.getSimpleName}"):

        val instance = MultipleValuesWithinRange(Some(1), Some(2), Some(3))

        describe("and all fields are filled properly"):
          val result = sut.validate(using instance)
          it("should be valid"):
            result.shouldBeValid()

        describe("and the first number is not defined"):
          val result = sut.validate(using instance.copy(firstNumber = None))
          it("should have a validation error for the firstNumber field only"):
            result shouldHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "secondNumber"
            result shouldNotHaveValidationErrorFor "thirdNumber"

        describe("and the second number is not defined"):
          val result = sut.validate(using instance.copy(secondNumber = None))
          it("should have a validation error for the secondNumber field only"):
            result shouldHaveValidationErrorFor "secondNumber"
            result shouldNotHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "thirdNumber"

        describe("and the third number is not defined"):
          val result = sut.validate(using instance.copy(thirdNumber = None))
          it("should have a validation error for the thirdNumber field only"):
            result shouldHaveValidationErrorFor "thirdNumber"
            result shouldNotHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "secondNumber"

        describe("and the first number is not positive"):
          val result = sut.validate(using instance.copy(firstNumber = Some(0)))
          it("should have a validation error for the firstNumber field only"):
            result shouldHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "secondNumber"
            result shouldNotHaveValidationErrorFor "thirdNumber"

        describe("and the second number is not positive"):
          val result = sut.validate(using instance.copy(secondNumber = Some(0)))
          it("should have a validation error for the secondNumber field only"):
            result shouldHaveValidationErrorFor "secondNumber"
            result shouldNotHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "thirdNumber"

        describe("and the third number is not positive"):
          val result = sut.validate(using instance.copy(thirdNumber = Some(0)))
          it("should have a validation error for the thirdNumber field only"):
            result shouldHaveValidationErrorFor "thirdNumber"
            result shouldNotHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "secondNumber"

        describe("and the first number is greater than 100"):
          val result = sut.validate(using instance.copy(firstNumber = Some(101)))
          it("should have a validation error for the firstNumber field only"):
            result shouldHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "secondNumber"
            result shouldNotHaveValidationErrorFor "thirdNumber"

        describe("and the second number is greater than 100"):
          val result = sut.validate(using instance.copy(secondNumber = Some(101)))
          it("should have a validation error for the secondNumber field only"):
            result shouldHaveValidationErrorFor "secondNumber"
            result shouldNotHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "thirdNumber"

        describe("and the third number is greater than 100"):
          val result = sut.validate(using instance.copy(thirdNumber = Some(101)))
          it("should have a validation error for the thirdNumber field only"):
            result shouldHaveValidationErrorFor "thirdNumber"
            result shouldNotHaveValidationErrorFor "firstNumber"
            result shouldNotHaveValidationErrorFor "secondNumber"

  def neverCalled(): Unit =

    given source: ValuesMap            = ValuesMap.empty
    val a: ResultWithoutData           = Valid
    val b: ResultWithSuccessData[Long] = Valid(1L)
    val bindingResult                  = BindingPass(BigDecimal(1))
    val c: BindingAndValidationResultWithoutData[BigDecimal] =
      Success(BigDecimal(1))(bindingResult)
    val d: BindingAndValidationResultWithSuccessData[BigDecimal, String] =
      SuccessWithData(BigDecimal(1), "a")(bindingResult)
    val e: ValidationResult = Valid

    /*
   This is just sort of a compile time test to ensure binding validation summaries can be unapplied properly
   and are exhaustive matches.
     */

    a match
      case Valid      =>
      case Invalid(_) =>

    b match
      case Valid(_)   =>
      case Invalid(_) =>

    c match
      case Success(_) =>
      case Failure(_) =>

    d match
      case SuccessWithData(_, _) =>
      case Failure(_)            =>

    e match
      case Invalid(_) =>
      case Valid      =>
      case Valid(_)   =>
