<p align="center"><a href="Table-of-Contents.md">Table of Contents</a></p>
<p align="center"><a href="Validation-metadata.md">Validation Metadata</a> &lt; Values Map to Object Binding &gt; <a href="Custom-binders.md">Custom Binders</a></p>

Aside from validation, a common operation is converting input data into a typed object and then validating it.
SValidator provides a binding system that supports both values maps and JSON sources.

Binding sources
===============

Binding operates on a `Source`:

- `ValuesMap` for `Map[String, Seq[String]]`
- `JsonCursor` for JSON strings

The main binding entry point is `ObjectBinder.bind`, which uses a `TypedBinder[A]` to bind an instance of `A`.

```scala
import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.special.ObjectBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.validation.Localizer

case class Person(firstName: String, lastName: String, age: Int) derives com.magaran.svalidator.binding.binders.TypedBinder

given BindingConfig = BindingConfig.default
given BindingContext = BindingContext.empty
given Localizer = Localizer.NoLocalization

val dataMap = Map("firstName" -> Seq("John"), "lastName" -> Seq("Smith"), "age" -> Seq("15"))
val source = ValuesMap(dataMap)

val result = ObjectBinder.bind[Person](source, FieldKey.Root)
```

Binding results
===============

The result is a `BindingResult[A]`:

- `BindingPass(value)` on success
- `BindingFailure(fieldErrors, cause)` on failure

You can pattern match to get the value or errors.

TypedBinder
===========

`TypedBinder[A]` is the type class used by the binding system. SValidator includes givens for primitives,
collections (`Option`, `List`, `Vector`, `Array`, `Set`) and can derive binders for case classes and stable enums.

You can derive binders by:

```scala
case class Person(firstName: String, lastName: String, age: Int) derives TypedBinder
```

or by providing a `given`:

```scala
given TypedBinder[Person] = TypedBinder.derived
```

For JSON binding, use `bindJson`:

```scala
val json = """{"firstName":"John","lastName":"Smith","age":15}"""
val result = ObjectBinder.bindJson[Person](json, FieldKey.Root)
```

Up next: [Custom binders](Custom-binders.md)
