<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Values-map-to-object-binding.md">Values Map to Object Binding</a> &lt; Custom Binders &gt; <a href="Binding-validators.md">Binding Validators</a></p>

In most cases you can derive binders automatically for case classes or stable enums. However, you can also implement
`TypedBinder[A]` for custom types or performance-sensitive cases.

Implementing a binder
=====================

A custom binder must implement `TypedBinder[A]` and return a `BindingPass` or `BindingFailure`.

```scala
import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig

final case class ZipCode(value: String)

given TypedBinder[ZipCode] with
  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[ZipCode] =
    given Source = source
    source match
      case map: ValuesMap =>
        fieldKey match
          case nested: NestedFieldKey =>
            map.get(nested) match
              case Some(values) if values.headOption.exists(_.trim.nonEmpty) =>
                BindingPass(ZipCode(values.head.trim))
              case Some(_) =>
                BindingFailure(fieldKey, "invalid.zipcode", None)
              case None =>
                BindingFailure(fieldKey, "required.field", Some(NoSuchElementException()))
          case FieldKey.Root =>
            BindingFailure(fieldKey, "invalid.root.binding", None)
      case _ =>
        BindingFailure(fieldKey, "invalid.source", None)
```

Notes:

- Missing values should be reported with `NoSuchElementException` as the cause when appropriate. This allows the
  `Option[A]` binder to treat missing values as `None`.
- If you provide a binder for a type that already has one, the new binder will be used from that point forward.

Up next: [Binding validators](Binding-validators.md)
