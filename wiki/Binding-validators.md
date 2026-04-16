<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Custom-binders.md">Custom Binders</a> &lt; Binding Validators &gt; <a href="Binding-configuration.md">Binding Configuration</a></p>

Often you want to perform binding and validation together. SValidator provides binding validators to handle this flow.

BindingValidator
================

Extend `BindingValidator[A]` to bind and validate a type `A` in one step.

```scala
import com.magaran.svalidator.validation.binding.BindingValidator
import com.magaran.svalidator.validation.ResultWithoutData

class PersonBindingValidator extends BindingValidator[Person] {
  override def validate(using instance: Person): ResultWithoutData = WithRules(
    For(_.firstName) ForField "firstName"
      must (_.nonEmpty) withMessage "First name is required"
  )
}
```

You can then bind and validate from a `Source` or JSON string:

```scala
val summary = personBindingValidator.bindAndValidate(source)
val summaryFromJson = personBindingValidator.bindAndValidate(jsonString)
```

MappingBindingValidator
=======================

If you want to bind a type `B`, map it to `A`, and then validate `A`, use `MappingBindingValidator`:

```scala
import com.magaran.svalidator.validation.binding.MappingBindingValidator

class PersonMappingValidator extends MappingBindingValidator[Person] {
  override def validate(using instance: Person) = WithRules(/* rules */)
}

val summary = new PersonMappingValidator
  .bindAndValidate[PersonForm](source, form => form.toPerson)
```

Results
=======

Both `bindAndValidate` methods return a `BindingAndValidationResult`:

- `Success(instance)` on successful bind+validate
- `Failure(failures)` when binding or validation fails

Up next: [Binding configuration](Binding-configuration.md)
