package integration.com.magaran.svalidator.validation.binding

import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.BindingContext
import com.magaran.svalidator.binding.BindingPass
import com.magaran.svalidator.binding.ValuesMap
import com.magaran.svalidator.testing.extensions.binding.withoutdata.shouldHaveValidationErrorFor
import com.magaran.svalidator.validation.binding.BindingValidator
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.validation.ResultWithoutData
import testUtils.asKey
import testUtils.Observes

case class ATestingClass(
  aString: String,
  anInt: Int,
  aFloat: Float,
  aDecimal: BigDecimal,
  anOptionalDouble: Option[Double],
  anOptionalString: Option[String]
) derives TypedBinder

class ATestingClassValidator extends BindingValidator[ATestingClass]:

  def validate(using ATestingClass): ResultWithoutData = WithRules(
    RuleFor { _.aString } ForField "aString"
      must { _.length >= 6 } withMessage "A string must have at least 6 characters",
    RuleFor { _.anInt } ForField "anInt"
      must { _ > 8 } withMessage "An int must be greater than 8"
  )

class BindingValidatorSpecs extends Observes:

  private val sut: BindingValidator[ATestingClass] = ATestingClassValidator()
  private given bindingConfig: BindingConfig       = BindingConfig.default
  private given metadata: BindingContext           = BindingContext.empty
  private given localizer: Localizer               = Localizer.NoLocalization

  private given source: ValuesMap = ValuesMap(
    "aString".asKey          -> List("someString"),
    "anInt".asKey            -> List("90"),
    "aFloat".asKey           -> List("88.5"),
    "aDecimal".asKey         -> List("900.0000009"),
    "anOptionalDouble".asKey -> List("99.87"),
    "anOptionalString".asKey -> List("anotherString")
  )

  describe("when binding and validating a testing class and all values are provided and valid"):

    val result = sut.bindAndValidate(source)

    it("should have returned a valid summary with the correct instance and metadata fields"):
      val instance =
        ATestingClass("someString", 90, 88.5f, BigDecimal("900.0000009"), Some(99.87d), Some("anotherString"))
      result shouldEqual BindingPass(instance).asSuccess

  describe("when binding and validating a testing class and some invalid values are provided in the bind"):

    val keyToStrings = source.updated("anInt".asKey, List("90.9"))
    val result       = sut.bindAndValidate(keyToStrings)

    it("should have returned an error for the anInt field"):
      result shouldHaveValidationErrorFor "anInt"

  describe("when binding and validating a testing class and some invalid values are provided to the validation phase"):

    val result = sut.bindAndValidate(source.updated("anInt".asKey, List("5")).updated("aString".asKey, List("error")))

    it("should have returned the aString field"):
      result shouldHaveValidationErrorFor "aString"

    it("should have returned an error for the anInt field"):
      result shouldHaveValidationErrorFor "anInt"
