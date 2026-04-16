package com.magaran.svalidator.validation

import scala.annotation.targetName

import com.magaran.typedmap.TypedEntry
import com.magaran.typedmap.TypedKey
import com.magaran.typedmap.TypedMap

/** Contains user attached metadata about the validation process in a [[ValidationFailure]] */
final class ValidationMetadata private (private val wrapped: TypedMap) extends TypedMap:

  @targetName("addEntry")
  override def + [A](kv: TypedEntry[A]): ValidationMetadata = new ValidationMetadata(wrapped + kv)

  @targetName("removeEntry")
  override def - [A](key: TypedKey[A]): ValidationMetadata = new ValidationMetadata(wrapped - key)

  override def apply[A](key: TypedKey[A]): A = wrapped(key)

  override def get[A](key: TypedKey[A]): Option[A] = wrapped.get(key)

  override def keys: Iterable[TypedKey[?]] = wrapped.keys

  override def merge(another: TypedMap): TypedMap = wrapped.merge(another)

  /** Merges this metadata with another, returning a new metadata that contains the values of both.
    * If a key exists in both metadata, the value in the argument metadata will be used.
    */
  def merge(another: ValidationMetadata): ValidationMetadata =
    new ValidationMetadata(wrapped.merge(another.wrapped))

object ValidationMetadata:

  /** Returns an empty <code>ValidationMetadata</code> */
  val empty: ValidationMetadata = new ValidationMetadata(TypedMap.empty)

  /** Creates a <code>ValidationMetadata</code> from a sequence of typed entries
    *
    * @param entries Typed entries to use in the created <code>ValidationMetadata</code>
    */
  def apply(entries: TypedEntry[?]*): ValidationMetadata =
    new ValidationMetadata(TypedMap(entries.map(x => x.key.asInstanceOf[TypedKey[Any]] -> x.value)*))
