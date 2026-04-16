package com.magaran.svalidator.binding.binders.special

import scala.collection.mutable.ListBuffer

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.BinderUtils
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.FieldKey

/** Utility class to reuse logic for binding different types of sequences (Lists,Sets...) */
protected[special] object SequenceBinderUtils:

  def bindSequenceAndConvert[A, B[_]](
    wrappedBinder: TypedBinder[A],
    fieldKey: FieldKey,
    source: Source,
    sequenceConversion: Vector[A] => B[A],
  )(using config: BindingConfig)(using BindingContext, TypeShow[B[A]]): BindingResult[B[A]] =
    BinderUtils.nonRootBinding(source, fieldKey) { nestedKey =>
      given givenSource: Source = source

      val fieldErrors = ListBuffer[FieldError]()
      val validValues = Vector.newBuilder[A]
      source match
        case valuesMap: ValuesMap =>
          val nonIndexedFieldName = valuesMap.get(nestedKey)
          nonIndexedFieldName match
            case Some(values) =>
              values.zipWithIndex.toList
                .map:
                  case (value, index) =>
                    val indexedKey = nestedKey.indexed(index)
                    wrappedBinder.bind(ValuesMap(indexedKey -> List(value)), indexedKey)
                .foreach:
                  case x: BindingFailure       => fieldErrors.addAll(x.fieldErrors)
                  case BindingPass(validValue) => validValues.addOne(validValue)
            case None =>
              val indexedSequenceLeadingString = s"$nestedKey["
              val indexedSequenceEndTokenChar  = ']'
              val indexedKeys                  = valuesMap.keys.filter(_.fullKey.startsWith(indexedSequenceLeadingString))
              val indexes = indexedKeys
                .map(_.fullKey.replace(indexedSequenceLeadingString, "").split(indexedSequenceEndTokenChar).head.toInt)
                .toList
                .distinct
                .sorted
              indexes
                .map: i =>
                  wrappedBinder.bind(valuesMap, nestedKey.indexed(i))
                .foreach:
                  case BindingPass(boundValue)   => validValues.addOne(boundValue)
                  case BindingFailure(errors, _) => fieldErrors.addAll(errors)
        case cursor: JsonCursor =>
          val valuesOption = cursor.values
          valuesOption match
            case Some(values) =>
              values.zipWithIndex.foreach:
                case (json, index) =>
                  wrappedBinder.bind(new JsonCursor(json.hcursor), nestedKey.indexed(index)) match
                    case BindingPass(boundValue)   => validValues.addOne(boundValue)
                    case BindingFailure(errors, _) => fieldErrors.addAll(errors)
            case None =>
              val value = cursor.focus.map(_.toString())
              value.map(_.trim).filter(_.nonEmpty) match
                case Some(invalidValue) =>
                  BindingFailure(nestedKey, config.languageConfig.invalidSequenceMessage(nestedKey, invalidValue), None)
                case None => BindingPass(sequenceConversion(Vector.empty))
      if fieldErrors.isEmpty then BindingPass(sequenceConversion(validValues.result()))
      else BindingFailure(fieldErrors.toList, None)
    }
