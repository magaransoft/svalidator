<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Binding-validators.md">Binding Validators</a> &lt; Binding Configuration &gt; <a href="Type-Based-Enumerations.md">Stable Enums</a></p>

The binding system is configured through `BindingConfig`, which controls date/time formatting, string handling,
boolean parsing, and error messages.

BindingConfig
=============

`BindingConfig` is a trait with the following options:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `dateFormatter` | `DateTimeFormatter` | `ISO_LOCAL_DATE` | Format for `LocalDate` binding |
| `timeFormatter` | `DateTimeFormatter` | `ISO_LOCAL_TIME` | Format for `LocalTime` binding |
| `stringBindingStyle` | `StringBindingStyle` | `InvalidateFullWhitespacePreserveWhitespaces` | How whitespace-only strings are handled |
| `booleanValuesMapBindingStyle` | `BooleanValuesMapBindingStyle` | `BindMissingAsFalse` | How missing boolean values are treated in `ValuesMap` sources |
| `languageConfig` | `BindingLanguageConfig` | `BindingLanguageConfig.defaultConfig` | Error message configuration |

Use `BindingConfig.default` for standard behavior, or implement the trait to customize:

```scala
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.config.StringBindingStyle
import com.magaran.svalidator.config.BooleanValuesMapBindingStyle
import java.time.format.DateTimeFormatter

given BindingConfig with
  val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
  val stringBindingStyle = StringBindingStyle.InvalidateFullWhitespacePreserveWhitespaces
  val booleanValuesMapBindingStyle = BooleanValuesMapBindingStyle.BindMissingAsFalse
  val languageConfig = BindingLanguageConfig.defaultConfig
```

BindingLanguageConfig
=====================

`BindingLanguageConfig` controls the error messages returned when binding fails. Implement the trait to customize
messages for each type:

```scala
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey
import com.magaran.svalidator.validation.MessageParts

given BindingLanguageConfig with
  def noValueProvidedMessage(fieldKey: FieldKey) =
    MessageParts("campo.requerido", Nil)
  def invalidIntegerMessage(fieldKey: NestedFieldKey, fieldValue: String) =
    MessageParts("entero.invalido", List(fieldValue))
  // ... override other methods as needed
```

The default config uses keys like `"invalid.value"`, `"required.field"`, etc. See the
[Localization](Localization.md) page for how these keys work with the `Localizer`.

IndexedFieldNameStyle
=====================

`IndexedFieldNameStyle` controls how indexed fields (arrays/lists) appear in error field names. Currently, the
`MixedNotation` style is used, which produces field names like:

- `addresses[0]` — indexed access
- `addresses[0].city` — nested field within an indexed element
- `person.addresses[2].city` — deeply nested

Up next: [Stable enums](Type-Based-Enumerations.md)
