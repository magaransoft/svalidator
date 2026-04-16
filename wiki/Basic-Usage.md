<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Getting-started.md">Getting Started</a> &lt; Basic Usage &gt; <a href="Functional-validators.md">Functional Validators</a></p>

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
=====================

To validate instances of the class, create a validator that extends `SimpleValidator[A]` where `A` is the type being
validated.

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
      must (_ >= 0) withMessage "Must be a positive number"
  )
}
```

`WithRules` receives a varargs list of `RuleBuilder` instances. Each chain starts with `For` and must set a field name
using `ForField`. Use `must`/`mustNot` and end each rule with `withMessage`. If a rule fails, further rules in the same
chain will not be evaluated.

If you need to validate an arbitrary expression (not a simple property selector), use `RuleFor` instead of `For` and
manually provide the field name.

```scala
override def validate(using instance: Person) = WithRules(
  RuleFor(p => p.firstName + " " + p.lastName) ForField "fullName"
    must (_.length <= 64) withMessage "Full name is too long"
)
```

Up next: [Functional validators](Functional-validators.md)
