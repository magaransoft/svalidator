package integration.com.magaran.svalidator.validation.binding

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.binding.ValuesMap
import com.magaran.svalidator.testing.extensions.binding.withoutdata.shouldHaveValidationErrorFor
import com.magaran.svalidator.validation.binding.MappingBindingValidator
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.ResultWithoutData
import testUtils.asKey
import testUtils.Observes

case class ADifferentTestingClass(
  aString: String,
  anInt: Int,
  aFloat: Float,
  aDecimal: BigDecimal,
  anOptionalDouble: Option[Double],
  anOptionalString: Option[String]
) derives TypedBinder

case class AMappedTestingClass(
  aMappedString: String,
  aMappedInt: Int,
  aMappedFloat: Float,
  aMappedDecimal: BigDecimal,
  aMappedOptionalDouble: Option[Double],
  aMappedOptionalString: Option[String]
) derives TypedBinder

class AMappedTestingClassValidator extends MappingBindingValidator[AMappedTestingClass]:

  def validate(using AMappedTestingClass): ResultWithoutData = WithRules(
    RuleFor { _.aMappedString } ForField "aString"
      must { _.contains("K") } withMessage "A string must contain at least a 'K'",
    RuleFor { _.aMappedInt } ForField "anInt"
      must { _ > 8 } withMessage "An int must be greater than 8"
  )

class MappingBindingValidatorSpecs extends Observes:

  private given config: BindingConfig                   = BindingConfig.default
  private given metadata: BindingContext                = BindingContext.empty
  private given localizer: Localizer                    = Localizer.NoLocalization
  val sut: MappingBindingValidator[AMappedTestingClass] = AMappedTestingClassValidator()

  private given source: ValuesMap = ValuesMap(
    "aString".asKey          -> List("someString"),
    "anInt".asKey            -> List("5"),
    "aFloat".asKey           -> List("88.5"),
    "aDecimal".asKey         -> List("900.0000009"),
    "anOptionalDouble".asKey -> List("99.87"),
    "anOptionalString".asKey -> List("anotherString")
  )

  val mapOp: ADifferentTestingClass => AMappedTestingClass = x =>
    AMappedTestingClass(
      x.aString + "SomethingWithTheLetterK",
      x.anInt + 1000,
      x.aFloat,
      x.aDecimal,
      x.anOptionalDouble,
      x.anOptionalString
    )

  describe("when binding and validating a testing class and all values are provided and valid"):

    val result = sut.bindAndValidate(source, mapOp)

    it("should have returned the a valid summary with the proper values"):
      val instance = AMappedTestingClass(
        "someStringSomethingWithTheLetterK",
        1005,
        88.5f,
        BigDecimal("900.0000009"),
        Some(99.87d),
        Some("anotherString")
      )
      result should equal(BindingPass(instance).asSuccess)

  describe("when binding and validating a testing class and some invalid values are provided in the bind"):

    val result = sut.bindAndValidate(source.updated("anInt".asKey, List("90.9")), mapOp)

    it("should have returned an error for the anInt field"):
      result shouldHaveValidationErrorFor "anInt"

  describe("when binding and validating a testing class and some invalid values are provided to the validation phase"):

    val result = sut.bindAndValidate(source.updated("anInt".asKey, List("-2000")), mapOp)

    it("should have returned an error for the anInt field"):
      result shouldHaveValidationErrorFor "anInt"
