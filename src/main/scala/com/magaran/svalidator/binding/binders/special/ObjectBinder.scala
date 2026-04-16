package com.magaran.svalidator.binding.binders.special

import scala.reflect.ClassTag

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingEventRegistry
import com.magaran.svalidator.binding.events.*
import com.magaran.svalidator.validation.Localizer
import com.magaran.svalidator.FieldKey

protected[svalidator] object ObjectBinder extends ObjectBinder(fireEvents = false)

/** Binds instances by pulling in the typed binder for the required type passed in the generic */
sealed class ObjectBinder protected[svalidator] (fireEvents: Boolean):

  def this() = this(fireEvents = true)

  /** Binds a concrete instance of <code>A</code> using the given [[Source]] and [[FieldKey]].
    *
    * @param  source        Source of values to use during binding
    * @param fieldKey Prefix to be prepended to all field names when scanning for values
    * @tparam A Type being bound
    * @return [[BindingPass]] with the bound value if successful, [[BindingFailure]] with errors and throwable cause otherwise
    */
  def bind[A](source: Source, fieldKey: FieldKey)(
    using binder: TypedBinder[A],
    config: BindingConfig,
    context: BindingContext,
    localizer: Localizer,
    tag: ClassTag[A],
  ): BindingResult[A] =
    fireEventIfEnabled(BeforeBindingEvent(source, fieldKey, tag))
    source match
      case invalid: InvalidJsonCursor =>
        BindingEventRegistry.publishEvent(FailedJsonParsingEvent(invalid.asException))
        BindingFailure(
          List(
            FieldError(
              fieldKey,
              config.languageConfig.invalidJsonMessage(fieldKey, invalid.receivedString),
              alreadyLocalized = false
            )
          ),
          Some(invalid.failure)
        )(using source)
      case source: Source =>
        val normalizedSource = source.normalize
        val result           = binder.bind(normalizedSource, fieldKey)
        result match
          case pass: BindingPass[A]   => fireEventIfEnabled(SuccessfulBindingEvent(pass, tag))
          case failed: BindingFailure => fireEventIfEnabled(FailedBindingEvent(failed, tag))
        result.localize

  /** Binds an instance of <code>A</code> using a [[JsonCursor]] created by parsing the given <code>jsonString</code>
    *
    * If the string is not valid json, a binding failure with the parsing failure as the cause is returned.
    *
    * @param jsonString The json string to parse and use for binding
    * @param fieldKey Field key being bound
    * @param binder Binder used to bind instances of <code>A</code>
    * @param config Binding config to use during binding
    * @param context Binding context information for this operation
    * @param localizer Localizer for binding failure messages
    * @param tag ClassTag for the bound type
    * @tparam A The type being bound
    * @return [[BindingPass]] with the bound value if successful, [[BindingFailure]] with details about the failure otherwise.
    */
  def bindJson[A](
    jsonString: String,
    fieldKey: FieldKey
  )(using TypedBinder[A], BindingConfig, BindingContext, Localizer, ClassTag[A]): BindingResult[A] =
    bind(JsonCursor(jsonString).fold(identity, identity), fieldKey)

  private def fireEventIfEnabled(event: BindingEvent): Unit =
    if fireEvents then BindingEventRegistry.publishEvent(event)
