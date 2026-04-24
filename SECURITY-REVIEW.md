# Security Review — svalidator

**Date**: 2026-04-23
**Version reviewed**: 0.1.1-SNAPSHOT (commit on main at time of review)
**Reviewer**: Claude Opus 4.6 + manual source verification

---

## Overview

svalidator is a Scala 3 compile-time-heavy validation and data binding library. Most logic
lives in macros that generate code at compile time, which is inherently a good security posture.
The runtime surface area is relatively small. No critical exploitable vulnerabilities were found,
but several design issues warrant attention.

---

## Findings

### 1. Mutable State in `Invalid` Case Class — Thread Safety

**Severity**: Medium
**File**: `src/main/scala/com/magaran/svalidator/validation/ValidationResult.scala`
**Lines**: 157-177

**Problem**: `Invalid` is a `case class` (users expect immutability) but contains a mutable
`var _validationMetadata`. The `withMetadata` method creates a new `Invalid` instance and
mutates it before returning:

```scala
private var _validationMetadata: ValidationMetadata = ValidationMetadata.empty

def withMetadata(metadata: ValidationMetadata): Invalid =
  val next = Invalid(validationFailures)
  next._validationMetadata = metadata  // mutation after construction
  next
```

If `Invalid` instances are shared across threads (e.g., cached validation results in an async
web framework), the `var` introduces a data race — a thread could read
`_validationMetadata` before the writing thread completes the assignment.

**Suggested fix**: Make `_validationMetadata` a parameter instead of a `var`. Options:

1. Add a private constructor parameter with a default:
   ```scala
   final case class Invalid(
     validationFailures: ::[ValidationFailure],
     private val _validationMetadata: ValidationMetadata = ValidationMetadata.empty
   ) ...
   ```
   Then `withMetadata` returns `copy(_validationMetadata = metadata)`.

2. Or use a secondary private case class that includes metadata, keeping the public API the same.

**Impact on tests**: `withMetadata` callers should continue to work unchanged if using `copy`.

---

### 2. Raw User Input in Exception Messages — Information Leakage

**Severity**: Low-Medium
**File**: `src/main/scala/com/magaran/svalidator/binding/exceptions/IllegalJsonCursorException.scala`
**Lines**: 10-16

**Problem**: The full raw JSON string that failed parsing is embedded in the exception message:

```scala
s"The received json string was $receivedJsonString"
```

If this exception propagates to an HTTP error response (common in Play/Tapir frameworks), the
entire request body could be leaked back to the client, potentially including sensitive data
from other fields (passwords, tokens, PII).

The `receivedString` field is also `val` (publicly accessible), and if the string is very
large (megabytes), it stays in memory as long as the exception object lives.

**Suggested fix**:

1. Truncate the string in the message: `receivedJsonString.take(200) + "..."`.
2. Keep the full string available via the `receivedString` field for server-side logging,
   but don't include it in `getMessage`.
3. Consider adding a size check — if the string is over some threshold, store only a truncated
   version.

---

### 3. Unchecked `.toInt` on Field Index in SequenceBinderUtils

**Severity**: Low-Medium
**File**: `src/main/scala/com/magaran/svalidator/binding/binders/special/SequenceBinderUtils.scala`
**Line**: 44

**Problem**: When parsing indexed field names from a `ValuesMap`, the code extracts what it
expects to be an integer index and calls `.toInt` without validation:

```scala
val indexes = indexedKeys
  .map(_.fullKey.replace(indexedSequenceLeadingString, "")
    .split(indexedSequenceEndTokenChar).head.toInt)  // unguarded .toInt
  .toList
  .distinct
  .sorted
```

If a malformed key like `items[abc]` or `items[9999999999]` is present in the source map:
- `"abc".toInt` throws `NumberFormatException` — unhandled, bubbles up as a 500 error
- An extremely large index could be used later to allocate oversized collections

**Suggested fix**:

```scala
val indexes = indexedKeys
  .flatMap { key =>
    val raw = key.fullKey.replace(indexedSequenceLeadingString, "")
      .split(indexedSequenceEndTokenChar).head
    raw.toIntOption  // returns None for non-numeric strings
  }
  .toList
  .distinct
  .sorted
```

Optionally add an upper bound check (e.g., `index < 10_000`) to prevent memory abuse.

**Testing**: Add a test case with a non-numeric indexed key (e.g., `"items[abc]"`) and verify
it produces a `BindingFailure` instead of an exception.

---

### 4. Structural Reflection in StableEnumBinder

**Severity**: Low
**File**: `src/main/scala/com/magaran/svalidator/binding/binders/special/StableEnumBinder.scala`
**Lines**: 3, 21-23

**Problem**: Uses `reflectiveSelectable` to access `.id` on enum values at runtime:

```scala
import scala.reflect.Selectable.reflectiveSelectable

private val valuesMap: Map[Int, A] = enumValues
  .map: value =>
    value.asInstanceOf[{ def id: Int }].id -> value
  .toMap
```

This bypasses compile-time type checking for the `.id` access. While the macro in
`TypedBinderMacros.deriveStableEnum` validates the field exists at compile time, the
`StableEnumBinder` class itself is not `private` — a consumer could construct it manually
with arbitrary data, causing a runtime `NoSuchMethodException`.

**Suggested fix**: Restrict the constructor visibility:

```scala
class StableEnumBinder[A] private[svalidator] (enumValues: Array[A])(using TypeShow[A])
    extends TypedBinder[A]
```

This ensures only the macro-generated code can create instances.

---

### 5. InvalidJsonCursor Throws on Every Method

**Severity**: Low
**File**: `src/main/scala/com/magaran/svalidator/binding/Source.scala`
**Lines**: 141-154

**Problem**: `InvalidJsonCursor` extends `JsonCursor` but throws on every operation:

```scala
override def delete: JsonCursor = throw asException
override def as[A: Decoder]: Result[A] = throw asException
override def focus: Option[Json] = throw asException
override def values: Option[Iterable[Json]] = throw asException
override def downField(fieldName: String): JsonCursor = throw asException
```

`JsonCursor.apply` returns `Either[InvalidJsonCursor, JsonCursor]`, so callers *should*
handle the `Left` case. But if an `InvalidJsonCursor` is passed around typed as `JsonCursor`,
the type system does not protect against calling these methods.

**Suggested fix**: This is a design choice and may be intentional (fail-fast). If keeping it,
document clearly that `JsonCursor` instances from `JsonCursor.apply` must always be
pattern-matched. Alternatively, consider a sealed trait approach where `InvalidJsonCursor` is
not a subtype of `JsonCursor` at all — forcing the caller to handle the error before accessing
cursor methods.

---

### 6. `MessageParts.message` Uses `String.format` — Developer Footgun

**Severity**: Low
**File**: `src/main/scala/com/magaran/svalidator/validation/MessageParts.scala`
**Line**: 13

**Problem**:

```scala
def message: String = messageKey.format(messageFormatValues*)
```

`messageFormatValues` is `Seq[Any]` and `messageKey` uses Java's `String.format`. If a
validator author provides a `messageKey` with format specifiers that don't match the values
(e.g., `"%s %s"` with only one value), this throws `MissingFormatArgumentException` at
runtime.

**Risk assessment**: This is NOT directly exploitable because `messageKey` is set by the
validator author (the developer using the library), not by end-user input. However, if
`messageKey` comes from a localization/i18n file that accepts user-contributed translations,
format string injection becomes possible (reading stack memory via `%x`, DoS via mismatched
specifiers).

**Suggested fix**: Consider using indexed parameters (`{0}`, `{1}`) with `MessageFormat`
instead of `%s`-style format strings, which is both safer and more i18n-friendly. Or document
prominently that `messageKey` must never contain untrusted content.

---

### 7. Non-Tail-Recursive `mergeNonEmptyLists`

**Severity**: Low
**File**: `src/main/scala/com/magaran/svalidator/validation/ValidationResult.scala`
**Lines**: 183-186

**Problem**:

```scala
private def mergeNonEmptyLists(a: ::[ValidationFailure], b: ::[ValidationFailure]): ::[ValidationFailure] =
  a match
    case head :: Nil => ::(head, b)
    case ::(head, ::(nextHead, nextTail)) => ::(head, mergeNonEmptyLists(::(nextHead, nextTail), b))
```

Not `@tailrec`, allocates a stack frame per element in `a`. With thousands of validation
failures merged together (e.g., validating a large CSV where every row fails), this could
cause `StackOverflowError`.

**Suggested fix**: Rewrite iteratively:

```scala
private def mergeNonEmptyLists(a: ::[ValidationFailure], b: ::[ValidationFailure]): ::[ValidationFailure] =
  val buffer = a.toList ::: b.toList
  buffer match
    case head :: tail => ::(head, tail)
    case _            => b  // unreachable given non-empty inputs
```

Or simply use list concatenation since both inputs are guaranteed non-empty.

---

### 8. Dependency Versions — Slightly Dated circe

**Severity**: Low
**File**: `build.sbt`
**Line**: 64

**Problem**: circe is pinned to `0.14.5`. The latest 0.14.x release is 0.14.10+. No known
critical CVEs for 0.14.5, but keeping the JSON parsing library current is good practice since
it handles untrusted input.

**Suggested fix**: Bump to latest 0.14.x. Run tests to verify compatibility.

---

## Positive Findings

- **No ReDoS risk**: No user-supplied regex patterns. Only hardcoded `"\\."` for splitting.
- **No injection vectors**: No SQL, shell execution, eval, or dynamic class loading.
- **No secrets in repo**: GPG fingerprint in `build.sbt` is a public key identifier.
- **CI is well-secured**: GitHub Actions pinned to commit SHAs, permissions restricted to
  `contents: read`, no secrets exposed.
- **All dependencies pinned**: No version ranges or wildcards.
- **Macros catch errors at compile time**: Most type-safety issues are caught before runtime.

---

## Summary Table

| # | Finding | Severity | Category | Key File |
|---|---------|----------|----------|----------|
| 1 | Mutable `var` in `Invalid` case class | Medium | Thread safety | ValidationResult.scala:157 |
| 2 | Raw JSON in exception message | Low-Medium | Info leakage | IllegalJsonCursorException.scala:14 |
| 3 | Unchecked `.toInt` on field index | Low-Medium | Unhandled exception | SequenceBinderUtils.scala:44 |
| 4 | Structural reflection in StableEnumBinder | Low | Type safety | StableEnumBinder.scala:23 |
| 5 | InvalidJsonCursor throws on all methods | Low | Design | Source.scala:141 |
| 6 | String.format footgun in MessageParts | Low | Consumer misuse | MessageParts.scala:13 |
| 7 | Non-tail-recursive mergeNonEmptyLists | Low | Stack overflow | ValidationResult.scala:183 |
| 8 | Slightly dated circe version | Low | Dependencies | build.sbt:64 |
