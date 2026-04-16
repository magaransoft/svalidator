<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Validating-complex-properties.md">Validating Complex Properties</a> &lt; Validating Multiple Fields &gt; <a href="Conditional-validation.md">Conditional Validation</a></p>

When multiple fields share the same type and validation rules, you can use `ForMultiple` to avoid repeating yourself.

ForMultiple
===========

`ForMultiple` applies the same rules to several fields at once. Each field gets its own error messages under its own
field name.

```scala
class RangeValidator extends SimpleValidator[Range] {

  override def validate(using Range) = WithRules(
    ForMultiple(_.min, _.max, _.default)
      must (_.isDefined) withMessage "Value is required"
      map (_.get)
      must (_ > 0) withMessage "Must be a positive number"
      must (_ < 100) withMessage "Must be less than 100"
  )
}
```

ForMultipleOptional
===================

Works like `ForOptional` but across multiple `Option[A]` fields. Rules only run when the option is defined.

```scala
ForMultipleOptional(_.nickname, _.middleName)
  must (_.length <= 32) withMessage "Must have 32 characters or less"
```

ForMultipleEach
===============

Works like `ForEach` but across multiple `Seq[A]` fields. Validates each element and indexes errors.

```scala
ForMultipleEach(_.primaryScores, _.secondaryScores)
  must (_ >= 0) withMessage "Scores must be non-negative"
```

RuleFor variants
================

If you need to use arbitrary expressions instead of property selectors, use the `RuleFor` equivalents with `Field()`:

```scala
private val minField = Field(_.min)
private val maxField = Field(_.max)

override def validate(using Range) = WithRules(
  RuleForMultiple(minField, maxField)
    must (_.isDefined) withMessage "Value is required"
)
```

`RuleForMultipleOptional` and `RuleForMultipleEach` work the same way.

Up next: [Conditional validation](Conditional-validation.md)
