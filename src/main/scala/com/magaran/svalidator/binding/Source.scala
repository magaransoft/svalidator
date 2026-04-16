package com.magaran.svalidator.binding

import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.exceptions.IllegalJsonCursorException
import com.magaran.svalidator.NestedFieldKey
import io.circe.ACursor
import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.ParsingFailure

/** Represents a source of field values to bind instances from.
  *
  * Can be a [[ValuesMap]] or a [[JsonCursor]]
  */
sealed trait Source:

  /** Navigates down the field structure of this source so that operations on a given field name act
    * on the children of the subField that was navigated to
    *
    * @param subFieldName The field to navigate into
    * @return A source pointing towards the navigated subfield
    */
  def downField(subFieldName: String): Source

  /** If normalization of field keys is necessary for this source, this method normalizes all keys of it to
    * match the indexing style of the passed in config, otherwise returns this source unchanged
    */
  def normalize(using BindingConfig): Source

/** Represents a [[Source]] where each field is acquired by its [[NestedFieldKey.fullKey]], and
  * returns the list of values provided as a sequence of <code>String</code>, if present
  *
  * @param valuesMap The underlying map of keys to values to use
  */
final class ValuesMap(valuesMap: Map[String, Seq[String]]) extends Source:

  def keys: Iterable[NestedFieldKey] = valuesMap.keys.map(NestedFieldKey.apply)

  def downField(fieldName: String): ValuesMap = this

  def get(key: NestedFieldKey): Option[Seq[String]] = valuesMap.get(key.fullKey)

  def iterator: Iterator[(NestedFieldKey, Seq[String])] =
    valuesMap.iterator.map:
      case (fullKey, value) => NestedFieldKey(fullKey) -> value

  def size: Int = valuesMap.size

  /** Returns a new ValuesMap with the given key/value pair added
    *
    * @param kv The key/value pair to add
    */
  def + (kv: (NestedFieldKey, List[String])): ValuesMap = ValuesMap(valuesMap + (kv._1.fullKey -> kv._2))

  /** Returns a new immutable ValuesMap updating this map with a given key/value mapping.
    *  @param    key the key
    *  @param    value the value
    */
  def updated(key: NestedFieldKey, value: List[String]): ValuesMap = ValuesMap(valuesMap.updated(key.fullKey, value))

  def + [V1 >: Seq[String]](kv: (NestedFieldKey, V1)): Map[NestedFieldKey, V1] =
    valuesMap
      .map:
        case (fullKey, value) => NestedFieldKey(fullKey) -> value
      .+(kv)

  def - (key: NestedFieldKey): ValuesMap =
    ValuesMap(valuesMap - key.fullKey)

  override def normalize(using config: BindingConfig): Source =
    ValuesMap(normalizeKeys(valuesMap))

  override def toString: String =
    val contents = valuesMap.map { case (key, value) => s"$key -> $value" }.mkString(", ")
    s"ValuesMap($contents)"

  private def normalizeKeys(valuesMap: Map[String, Seq[String]])(
    using config: BindingConfig
  ): Map[String, Seq[String]] =
    val indexingStyle = config.indexedFieldNameStyle
    valuesMap map:
      case (key, value) => indexingStyle.normalizeKey(key) -> value

/** Provides convenience methods for creating [[ValuesMap]] instances */
object ValuesMap:

  /** Provides an empty instance of [[ValuesMap]] */
  def empty: ValuesMap = ValuesMap(Map.empty)

  /** Creates a [[ValuesMap]] from the given key/value pairs
    *
    * @param values The key/value pairs to use
    */
  def apply(values: (NestedFieldKey, Seq[String])*): ValuesMap =
    ValuesMap(values.map {
      case (key, value) => key.fullKey -> value
    }.toMap)

  /** Creates a [[ValuesMap]] from the given map of key/value pairs
    *
    * @param values The map of key/value pairs to use
    */
  def apply(values: Map[String, Seq[String]]): ValuesMap = new ValuesMap(values)

/** Represents a [[Source]] where values are stored in an underlying JSON AST, and can be navigated through
  * using the class' methods
  */
sealed class JsonCursor protected[svalidator] (jsonCursor: ACursor) extends Source:

  /** Deletes the field at the current position of the cursor */
  def delete = new JsonCursor(jsonCursor.delete)

  /** Attempts to parse the current position of the cursor as the given type with the given decoder */
  def as[A: Decoder]: Result[A] = jsonCursor.as[A]

  /** Returns the current position of the JSON cursor as a JSON AST */
  def focus: Option[Json] = jsonCursor.focus

  /** If the current focus is an array, returns an iterable of the JSON ASTs contained in it */
  def values: Option[Iterable[Json]] = jsonCursor.values

  /** Creates a new instance of this source with the cursor pointing to the field with the given
    * child field name
    *
    * @param fieldName The name of the field that the cursor will point to
    */
  def downField(fieldName: String): JsonCursor = new JsonCursor(jsonCursor.downField(fieldName))

  override def normalize(using BindingConfig): Source = this

  override def toString: String =
    s"JsonCursor(${jsonCursor.focus.getOrElse(Json.Null).toString()})"

/** Represents a [[JsonCursor]] source where the passed in json string that was parsed was not
  * a valid json string
  *
  * @param receivedString The invalid json String that was attempted to be parsed
  * @param failure The parsing failure exception that occurred when attempting to parse the json string
  */
final class InvalidJsonCursor protected[svalidator] (val receivedString: String, val failure: ParsingFailure)
    extends JsonCursor(Json.Null.hcursor):

  override def delete: JsonCursor = throw asException

  override def as[A: Decoder]: Result[A] = throw asException

  override def focus: Option[Json] = throw asException

  override def values: Option[Iterable[Json]] = throw asException

  override def downField(fieldName: String): JsonCursor = throw asException

  def asException: IllegalJsonCursorException = IllegalJsonCursorException(receivedString, failure)

/** Provides convenience methods for creating [[JsonCursor]] instances */
object JsonCursor:

  /** Creates a [[JsonCursor]] from the given json string
    *
    * @param jsonString The json string to parse
    */
  def apply(jsonString: String): Either[InvalidJsonCursor, JsonCursor] =
    io.circe.parser.parse(jsonString) match
      case Left(parsingFailure) =>
        Left(new InvalidJsonCursor(jsonString, parsingFailure))
      case Right(json) =>
        Right(new JsonCursor(json.hcursor))

  /** Creates a [[JsonCursor]] from the given key/value pair
    *
    * @param kv The key/value pair to use
    */
  protected[svalidator] def apply(kv: (NestedFieldKey, Json)): JsonCursor =
    new JsonCursor(build(kv._1.toList, kv._2).hcursor)

  private def build(key: List[NestedFieldKey], value: Json): Json =
    key match
      case Nil => value
      case NestedFieldKey(_, fieldName) :: xs =>
        val inner = build(xs, value)
        Json.obj(fieldName -> inner)
