<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Binding-configuration.md">Binding Configuration</a> &lt; Stable Enums &gt; <a href="Localization.md">Localization</a></p>

Stable Enums
============

SValidator can derive binders for Scala 3 enums that have a leading `id: Int` field. This is referred to as a stable
enum, and is supported by the `TypedBinder.derived` macro.

Example
=======

```scala
enum Status(val id: Int):
  case Active extends Status(1)
  case Disabled extends Status(2)
```

If you `derive` a `TypedBinder[Status]`, SValidator will:

- Read the `values` of the enum
- Match the incoming integer value against the enum's `id` field
- Bind the corresponding enum value or return a `BindingFailure`

```scala
given com.magaran.svalidator.binding.binders.TypedBinder[Status] =
  com.magaran.svalidator.binding.binders.TypedBinder.derived
```

If the value is missing, not an integer, or does not match any enum `id`, binding fails with the configured
`BindingLanguageConfig.invalidEnumerationMessage`.

Up next: [Localization](Localization.md)
