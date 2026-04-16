<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Conditional-validation.md">Conditional Validation</a> &lt; Map Transformations &gt; <a href="Advanced-rule-chaining.md">Advanced Rule Chaining</a></p>

In some cases it is useful to validate a value, then transform it, and continue validation on the transformed value.
Use `map` in the rule chain to do this once and only if previous rules succeeded.

```scala
class PersonValidator extends SimpleValidator[Person] {

  override def validate(using instance: Person) = WithRules(
    For(_.notes) ForField "notes"
      mustNot (_.isEmpty) withMessage "Notes can not be empty"
      map (_.get)
      must (x => x.nonEmpty && x.head.isLetter) withMessage "Notes must start with a letter"
  )
}
```

The mapping function:

- Runs only once.
- Runs only if all previous validations succeeded.
- Changes the type of the property for further rules in the chain.

Up next: [Advanced rule chaining](Advanced-rule-chaining.md)
