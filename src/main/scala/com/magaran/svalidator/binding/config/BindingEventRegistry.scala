package com.magaran.svalidator.binding.config

import java.util.concurrent.ConcurrentLinkedQueue

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.reflect.ClassTag

import com.magaran.svalidator.binding.events.BindingEvent
import com.magaran.svalidator.binding.BindingEventListener

class InitializedBindingEventRegistry protected[config] {

  protected[config] final val eventListeners: ConcurrentLinkedQueue[(Class[?], BindingEventListener[?])] =
    ConcurrentLinkedQueue[(Class[?], BindingEventListener[?])]()

  /**  Registers the given listener to receive events of the specified type
    *
    * @param listener Listener to register
    * @tparam A Type of event to listen for
    */
  def registerEventListener[A <: BindingEvent](listener: BindingEventListener[A])(
    using classTag: ClassTag[A]
  ): this.type =
    eventListeners.add(classTag.runtimeClass -> listener)
    this

  /** Removes all registered event listeners from this object */
  def clearListeners(): this.type =
    eventListeners.clear()
    this

}

/** Provides methods for initializing, registering or clearing binders, as well as registering listeners for binding events */
object BindingEventRegistry:

  private var initializedRegistry: Option[InitializedBindingEventRegistry] = None

  def initializeRegistry: InitializedBindingEventRegistry = {
    val registry = new InitializedBindingEventRegistry()
    initializedRegistry = Some(registry)
    registry
  }

  /** Publishes the given event to all registered listeners of the same type or its supertypes
    *
    * @param event Event to publish
    * @tparam A Type of binding event to publish
    */
  protected[svalidator] def publishEvent[A <: BindingEvent](event: A): Unit = {
    initializedRegistry.foreach { registry =>
      registry.eventListeners.asScala
        .collect:
          case (clazz, listener) if clazz.isAssignableFrom(event.getClass) =>
            listener.asInstanceOf[BindingEventListener[A]].handle(event)
    }
  }
