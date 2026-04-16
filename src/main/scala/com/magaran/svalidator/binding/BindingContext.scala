package com.magaran.svalidator.binding

import scala.annotation.targetName

import com.magaran.typedmap.TypedEntry
import com.magaran.typedmap.TypedKey
import com.magaran.typedmap.TypedMap

/** Contains information about the calling context of a binding process, in the form of a map of
  * typed values that can be used to pass information down to binders
  */
final class BindingContext private (private val wrapped: TypedMap) extends TypedMap:

  @targetName("addEntry")
  override def + [A](kv: TypedEntry[A]): BindingContext = new BindingContext(wrapped + kv)

  @targetName("removeEntry")
  override def - [A](key: TypedKey[A]): BindingContext = new BindingContext(wrapped - key)

  override def apply[A](key: TypedKey[A]): A = wrapped(key)

  override def get[A](key: TypedKey[A]): Option[A] = wrapped.get(key)

  override def keys: Iterable[TypedKey[?]] = wrapped.keys

  override def merge(another: TypedMap): TypedMap = wrapped.merge(another)

  /** Merges this context with another, returning a new context that contains the values of both contexts.
    * If a key exists in both contexts, the value in the argument context will be used.
    */
  def merge(another: BindingContext): BindingContext =
    new BindingContext(wrapped.merge(another.wrapped))

object BindingContext:

  val _empty = new BindingContext(TypedMap.empty)

  /** Returns an empty binding context */
  def empty: BindingContext = _empty

  /** Creates a binding context from a sequence of typed entries
    *
    * @param entries Typed entries to use in the created BindingContext
    */
  def apply(entries: TypedEntry[?]*): BindingContext =
    new BindingContext(TypedMap(entries.map(x => x.key.asInstanceOf[TypedKey[Any]] -> x.value)*))
