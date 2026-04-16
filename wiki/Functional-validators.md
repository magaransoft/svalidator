<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Basic-Usage.md">Basic Usage</a> &lt; Functional Validators &gt; <a href="Validating-complex-properties.md">Validating Complex Properties</a></p>

Functional validators
=====================

Functional validators are meant for cases where validation depends on input data (e.g. database results) or requires
an implicit context (e.g. request/session). The DSL is the same as `SimpleValidator`, but execution returns an
`Either[Invalid, SuccessData]` and is started with `WhenValidating`.

Syntax differences vs SimpleValidator
=====================================

- You call `validate(instance, inputData)(using context)` instead of `validate(using instance)`.
- You use `WhenValidating(instance).withSuccessData(...)` to start building the result.
- The result is `Either[Invalid, SuccessData]` instead of `Valid`/`Invalid`.

Example
=======

```scala
import com.magaran.svalidator.validation.Invalid
import com.magaran.svalidator.validation.functional.FunctionalValidator

case class RegistrationData(emailAlreadyExists: Boolean)
case class RequestContext(isAdmin: Boolean)

class PersonFunctionalValidator
  extends FunctionalValidator[Person, RegistrationData, RequestContext, Unit] {

  override def validate(instance: Person, inputData: RegistrationData)(using ctx: RequestContext)
      : Either[Invalid, Unit] =
    WhenValidating(instance)
      .withSuccessData(())
      .withRules(
        For(_.age) ForField "age"
          must (_ >= 0) withMessage "Must be a positive number",
        For(_.firstName) ForField "firstName"
          must (_.nonEmpty) withMessage "First name is required"
      )
}
```

Convenience classes
===================

The functional package provides aliases that fix some type parameters for common use cases:

- `InstanceValidator[Instance]`: no input data, no context, no success data.
- `InputValidator[Instance, InputData]`: input data only.
- `ContextualValidator[Instance, Context]`: implicit context only.

All of them extend `FunctionalValidator` and use the same DSL and execution flow.

Up next: [Validating complex properties](Validating-complex-properties.md)
