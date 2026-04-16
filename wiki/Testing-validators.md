<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Localization.md">Localization</a> &lt; Testing Validators &gt; <a href="Integration-with-Play!-framework.md">Integration with Play! Framework</a></p>

SValidator provides test extensions for all validator and binding result types.

Testing SimpleValidator results
===============================

For `SimpleValidator` results (`ResultWithoutData`), import from `validation.withoutdata`:

```scala
import com.magaran.svalidator.testing.extensions.validation.withoutdata.*

val summary = validator.validate(using instance)
summary.shouldBeValid()
summary.shouldHaveValidationErrorFor("firstName")
summary.shouldHaveValidationErrorFor("firstName").withMessageKey("required.field")
summary.shouldHaveValidationErrorFor("firstName").withMessageKey("required.field").withFormatValues("John")
summary.shouldNotHaveValidationErrorFor("lastName")
```

Testing results with success data
==================================

For validators that return `ResultWithSuccessData[A]`, import from `validation.withdata`:

```scala
import com.magaran.svalidator.testing.extensions.validation.withdata.*

val summary = validator.validate(using instance)
summary.shouldBeValid.withData(expectedData)
summary.shouldHaveValidationErrorFor("field")
summary.shouldNotHaveValidationErrorFor("field")
```

Testing FunctionalValidator results
====================================

Functional validators return `Either[Invalid, A]`. Import from `validation.functional`:

```scala
import com.magaran.svalidator.testing.extensions.validation.functional.*

val result = validator.validate(person, inputData)(using context)
result.shouldBeValid.withData(())
result.shouldHaveValidationErrorFor("age").withMessageKey("positive.number")
result.shouldNotHaveValidationErrorFor("firstName")
```

Testing binding results
=======================

For `BindingAndValidationResult`, import from `binding`:

```scala
import com.magaran.svalidator.testing.extensions.binding.*

val summary = bindingValidator.bindAndValidate(source)
shouldHaveValidationErrorFor(summary, "firstName").withMessageKey("required.field")
shouldNotHaveValidationErrorFor(summary, "lastName")
```

Testing component delegate validators
======================================

If you test validators that use `ForComponent`, mock the delegated validator's `validate` method to return `Valid`
or `Invalid` as appropriate, and verify it is invoked with the correct arguments. This keeps the test focused on
the current validator's behavior.

Up next: [Integration with Play! framework](Integration-with-Play!-framework.md)
