<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Advanced-rule-chaining.md">Advanced Rule Chaining</a> &lt; Validation Metadata &gt; <a href="Values-map-to-object-binding.md">Values Map to Object Binding</a></p>

You can attach typed metadata to validation failures using `withMetadata`. This is useful for error codes, severity
levels, or any structured data your error handling needs beyond message strings.

Defining metadata keys
======================

Metadata keys are instances of `TypedKey[A]` from the `typed-map` library:

```scala
import com.magaran.typedmap.TypedKey

object ErrorCode extends TypedKey[String]
object Severity extends TypedKey[Int]
```

Attaching metadata
==================

Use `withMetadata` after `withMessage` to attach a key-value pair to the failure:

```scala
class PersonValidator extends SimpleValidator[Person] {

  override def validate(using Person) = WithRules(
    For(_.lastName) ForField "lastName"
      must (_.nonEmpty) withMessage "Last name is required"
      withMetadata ErrorCode -> "ERR_1001",

    For(_.age) ForField "age"
      must (_ >= 0) withMessage "Must be a positive number"
      withMetadata ErrorCode -> "ERR_1002"
      withMetadata Severity -> 1
  )
}
```

You can also generate metadata dynamically based on the field value:

```scala
For(_.age) ForField "age"
  must (_ >= 0) withMessage "Must be positive"
  withMetadata (age => ErrorCode -> s"invalid_age_$age")
```

Reading metadata
================

Metadata is available on each `ValidationFailure` through the `metadata` property:

```scala
val result = validator.validate(using instance)
result match
  case invalid: Invalid =>
    for failure <- invalid.validationFailures do
      val code = failure.metadata.get(ErrorCode) // Option[String]
      val sev  = failure.metadata.get(Severity)  // Option[Int]
  case _ =>
```

Up next: [Values map to object binding](Values-map-to-object-binding.md)
