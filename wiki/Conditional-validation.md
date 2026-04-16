<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Validating-multiple-fields.md">Validating Multiple Fields</a> &lt; Conditional Validation &gt; <a href="Map-transformations.md">Map Transformations</a></p>

You can enforce validation of a field only if another field (or the instance as a whole) meets certain requirements.

```scala
override def validate(using instance: Person) = WithRules(
  For(_.married) ForField "married"
    must (_ == false) when (_.age < 18) withMessage "Must be 18 years or older to allow marking marriage",

  For(_.hasJob) ForField "hasJob"
    must (_ == false) when (_.age < 21) withMessage "Must be 21 years or older to allow marking a job"
)
```

The `when` clause applies only to the rule in the same chain line. To apply a condition to multiple rules, group them
with `When`:

```scala
class PersonValidator extends SimpleValidator[Person] {

  override def validate(using instance: Person) = WithRules(
    When(_.age < 21)(
      For(_.hasJob) ForField "hasJob"
        must (_ == false) withMessage "Must be 21 years or older to allow marking a job",
      For(_.married) ForField "married"
        must (_ == false) withMessage s"Can't be married at ${instance.age}. Must be 21 years or older"
    )
  )
}
```

Up next: [Map transformations](Map-transformations.md)
