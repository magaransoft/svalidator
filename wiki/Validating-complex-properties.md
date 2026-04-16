<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Functional-validators.md">Functional Validators</a> &lt; Validating Complex Properties &gt; <a href="Validating-multiple-fields.md">Validating Multiple Fields</a></p>

The `For` method allows validation of any property and assigns error messages to a field name. For complex structures,
`SimpleValidator` also supports `Option[A]`, `Seq[A]`, and delegating validation to other validators.

```scala
override def validate(using instance: Person) = WithRules(
  ForOptional(_.notes) ForField "notes"
    must (_.length <= 32) withMessage "Notes can't have more than 32 characters",

  ForEach(_.tasksCompletedByMonth) ForField "tasksCompletedByMonth"
    must (_ >= 0) withMessage "Must be a positive number",

  ForComponent(_.primaryAddress) ForField "primaryAddress"
    validateUsing new AddressValidator,

  ForOptionalComponent(_.emergencyPhoneNumber) ForField "emergencyPhoneNumber"
    validateUsing new PhoneNumberValidator,

  ForEachComponent(_.otherAddresses) ForField "otherAddresses"
    validateUsing new AddressValidator
)
```

- `ForOptional` validates only if the option is defined.
- `ForEach` validates each element of a sequence and indexes errors (e.g. `tasksCompletedByMonth[0]`).
- `ForComponent` delegates validation to another `Validator` and prefixes errors with the field name.
- `ForOptionalComponent` and `ForEachComponent` do the same for optional and sequence components.

Up next: [Validating multiple fields](Validating-multiple-fields.md)
