SValidator [![Tests &amp; Docs](https://github.com/magaransoft/svalidator/actions/workflows/ci.yml/badge.svg)](https://github.com/magaransoft/svalidator/actions/workflows/ci.yml)
==========

A library for validation and data binding of scala objects in a fluent and concise manner.

This project is heavily inspired by the [FluentValidation library for .NET](https://github.com/JeremySkinner/FluentValidation) and
licensed under the MIT license.

An [additional library](https://github.com/magaransoft/svalidator-play) is provided for easy integration with the [Play! framework](https://www.playframework.com/).

Installation
============

SValidator is published to Maven Central for Scala 3. Add the following to your `build.sbt`:

```scala
libraryDependencies += "com.magaran" %% "svalidator" % "0.1.0"
```

Quick Usage
===========

Consider the following case class.

```scala
case class Person(firstName: String,
                  lastName: String,
                  age: Int,
                  married: Boolean,
                  hasJob: Boolean,
                  notes: Option[String])
```

SimpleValidator style
---------------------

Create a class that inherits from `com.magaran.svalidator.validation.simple.SimpleValidator[A]` where A is
the type of the object you want to validate. Then define rules with `WithRules` and the fluent rule builders.

```scala
import com.magaran.svalidator.validation.simple.SimpleValidator

class PersonValidator extends SimpleValidator[Person] {

  override def validate(using instance: Person) = WithRules(

    For(_.firstName) ForField "firstName"
      must (_.nonEmpty) withMessage "First name is required"
      must (_.length <= 32) withMessage "Must have 32 characters or less",

    For(_.lastName) ForField "lastName"
      must (_.nonEmpty) withMessage "Last name is required"
      must (_.length <= 32) withMessage "Must have 32 characters or less",

    For(_.age) ForField "age"
      must (_ >= 0) withMessage "Must be a positive number",

    For(_.married) ForField "married"
      must (_ == false) when (_.age < 18) withMessage "Must be 18 years or older to allow marking marriage",

    For(_.hasJob) ForField "hasJob"
      must (_ == false) when (_.age < 21) withMessage "Must be 21 years or older to allow marking a job"
  )
}
```

To perform the actual validation, create an instance of your validator class and call `validate` passing in
the instance to validate.

```scala
val validator = new PersonValidator
val summary = validator.validate(using instance)
```

FunctionalValidator style
-------------------------

For cases where validation depends on additional input data or implicit context, use the functional style.
Extend `FunctionalValidator` (or one of its aliases) and return `Either[Invalid, SuccessData]`.

```scala
import com.magaran.svalidator.validation.Invalid
import com.magaran.svalidator.validation.functional.FunctionalValidator

// Example input/context types
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

To execute:

```scala
val validator = new PersonFunctionalValidator
val result = validator.validate(person, RegistrationData(emailAlreadyExists = false))(using RequestContext(false))
```

Check the wiki for more details: [Basic Usage](wiki/Basic-Usage.md)
