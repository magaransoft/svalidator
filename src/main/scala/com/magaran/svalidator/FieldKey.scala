package com.magaran.svalidator

import com.magaran.svalidator.config.IndexedFieldNameStyle
import com.magaran.svalidator.validation.FieldInfo

/** Identifies a field within a [[com.magaran.svalidator.binding.Source Source]] that is used to bind a value to a property of a model.
  *
  * The special value [[FieldKey.Root]] identifies the root field of the source, whereas other fields
  * are represented by [[NestedFieldKey]] instances.
  */
sealed trait FieldKey:

  /** Creates a child [[NestedFieldKey]] under this key with the given field name. */
  def downField(fieldName: String): NestedFieldKey = NestedFieldKey(this, fieldName)

/** Companion object providing factory methods for [[FieldKey]]. */
object FieldKey:

  /** Creates a [[FieldKey]] from a property accessor lambda, extracting the field name at compile time. */
  inline def of[A](inline extractor: A => Any): FieldKey =
    FieldInfo.apply[A, Any](extractor, "`FieldInfo`s constructor").fieldKey

  /** Special [[FieldKey]] value that targets the root field of the source */
  object Root extends FieldKey:

    override def toString: String = "(root)"

/** Represents [[FieldKey]]s of any non-root field
  *
  * @param parent The [[FieldKey]] that contains this key as a child. Can be [[FieldKey.Root]] or another <code>NestedFieldKey</code>
  * @param fieldName The string name of this field within its parent field
  */
sealed case class NestedFieldKey(parent: FieldKey, private val fieldName: String)
    extends FieldKey
    with Iterable[NestedFieldKey]:

  private def getHierarchy: List[NestedFieldKey] =
    parent match
      case FieldKey.Root => List(this)
      case x: NestedFieldKey =>
        this :: x.getHierarchy

  private lazy val hierarchy = getHierarchy.reverse

  /** Returns an iterator of each field name in this key's field hierarchy, excluding the root field */
  override def iterator: Iterator[NestedFieldKey] =
    hierarchy.iterator

  // We use this private val instead of doing it directly because Scaladoc will not link to vals with @see
  private val _fullKey = parent match
    case FieldKey.Root                 => fieldName
    case nonRootParent: NestedFieldKey => nonRootParent.fullKey + "." + fieldName

  /** Returns a string with the full key of field names of this key and its ancestors, excluding the root
    * field (e.g: "someField.someNestedField.someDeepNestedField" ).
    */
  def fullKey: String = _fullKey

  /** Returns a new key with the same field concatenated to the given index, according to the
    * passed in [[com.magaran.svalidator.config.IndexedFieldNameStyle]]
    *
    * @param index The index at which the new key will be indexed
    */
  def indexed(index: Int): NestedFieldKey =
    NestedFieldKey(parent, s"$fieldName${IndexedFieldNameStyle.MixedNotation.getIndexedInfoForIndex(index)}")

  /** Returns a string with the full key of field names of this key and its ancestors, excluding the root
    * field (e.g: "someField.someNestedField.someDeepNestedField" ).  Same as calling [[fullKey]]
    */
  override def toString: String = fullKey

/** Contains utility methods for creating [[NestedFieldKey]]s */
object NestedFieldKey:

  /**  Creates a [[NestedFieldKey]] from the given <code>fullKey</code>
    * @param fullKey The full key to parse
    *
    * @see [[NestedFieldKey.fullKey]]
    */
  def apply(fullKey: String): NestedFieldKey =
    val trimmed = fullKey.trim
    if trimmed.isEmpty || trimmed.startsWith("(") then
      throw IllegalArgumentException(
        s"Cannot create a nested field key from `$fullKey`.\n" +
          "Nested field keys cannot be empty or start with `(`".stripMargin
      )
    val dotNotationKey = trimmed.filter(_ != ']').replace('[', '.')
    val tokens         = dotNotationKey.split("\\.")
    tokens.tail.foldLeft(FieldKey.Root.downField(tokens.head)):
      case (parent, fieldName) if fieldName.exists(!_.isDigit) => parent.downField(fieldName)
      case (parent, fieldName) =>
        parent.indexed(fieldName.toInt)
