package com.magaran.svalidator.binding

import com.magaran.svalidator.binding.events.BindingEvent

/** Trait for classes that listen to binding events.
  *
  * You must call [[com.magaran.svalidator.binding.config.InitializedBindingEventRegistry.registerEventListener registerEventListener]]
  * on the initialized BindingEventRegistry to register for the given event(s) your implementation wants to listen to
  *
  * @tparam A Type of the binding event to listen to
  */
trait BindingEventListener[-A <: BindingEvent]:

  /**  Handles the given event
    *
    * @param event Event that was fired by the
    * [[com.magaran.svalidator.binding.config.BindingEventRegistry BindingEventRegistry]]
    */
  def handle(event: A): Unit
