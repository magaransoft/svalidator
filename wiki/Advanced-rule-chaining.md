<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Map-transformations.md">Map Transformations</a> &lt; Advanced Rule Chaining &gt; <a href="Validation-metadata.md">Validation Metadata</a></p>

SValidator provides several tools for building complex validation chains that go beyond simple `must`/`mustNot` rules.

Field and Key macros
====================

The `Field()` macro creates a reusable `FieldInfo` that captures both the property extractor and field name at compile
time. The `Key()` macro extracts just the `FieldKey`.

```scala
class PersonValidator extends SimpleValidator[Person] {
  private val firstNameField = Field(_.firstName)
  private val lastNameField  = Field(_.lastName)
  private val firstNameKey   = Key(_.firstName)

  override def validate(using Person) = WithRules(
    RuleForMultiple(firstNameField, lastNameField)
      must (_.nonEmpty) withMessage "Name is required"
  )
}
```

These are useful when you need to reference the same field in multiple rule chains or pass field references to
`RuleForMultiple`, `RuleForOptional`, etc.

andRuleFor
==========

`andRuleFor` switches the validation chain to a different property. If any preceding rule fails, everything after
`andRuleFor` is skipped. This lets you express dependent validations across fields in a single chain.

```scala
override def validate(using OptionalRange) = WithRules(
  For(_.min)
    must (_.isDefined) withMessage "Min must be defined"
    andRuleFor (_.max) ForField "max"
    must (_.isDefined) withMessage "Max must be defined"
    andRuleFor (x => (x.min.get, x.max.get)) ForField "range"
    must ((min, max) => min <= max) withMessage "Min must be <= max"
)
```

Note that `andRuleFor` requires a `ForField` call to set the field name for error reporting.

switchFieldTo
=============

`switchFieldTo` changes which field name errors are reported under, without changing the value being validated. Like
`andRuleFor`, if preceding rules fail, further rules are skipped.

```scala
override def validate(using OptionalRange) = WithRules(
  RuleFor { identity } ForField "min"
    must (_.min.isDefined) withMessage "Min must be defined"
    switchFieldTo "max"
    must (_.max.isDefined) withMessage "Max must be defined"
    switchFieldTo "range"
    must (x => x.min.get <= x.max.get) withMessage "Min must be <= max"
)
```

You can also pass a `FieldKey` instead of a string to `switchFieldTo`.

Up next: [Validation metadata](Validation-metadata.md)
