package com.magaran.svalidator.binding.binders.typed

import com.magaran.svalidator.binding.*
import com.magaran.svalidator.binding.binders.BinderUtils
import com.magaran.svalidator.binding.binders.TypedBinder
import com.magaran.svalidator.binding.config.BindingConfig
import com.magaran.svalidator.binding.config.BindingLanguageConfig
import com.magaran.svalidator.utils.TypeShow
import com.magaran.svalidator.validation.MessageParts
import com.magaran.svalidator.FieldKey
import com.magaran.svalidator.NestedFieldKey
import io.circe.Decoder

/** Base binder used to implement typed binders of proper types (non-higher kinded) that do not accept
  * binding values at the [[FieldKey.Root]] level.
  *
  * @tparam A The target type this binder wants to bind
  * @tparam B The type to decode from a json when using a Json Source
  * @param convertFromString Function used to bind the target value from a <code>String</code>
  * @param convertFromExtracted Function to convert the value into the target type after extracting from a Json.
  *                             Can be <code>identity</code> if the target type is the same as extracted from Json.
  * @param invalidMessageFunction Function from [[com.magaran.svalidator.binding.config.BindingLanguageConfig]] to use to report error messages of the type
  */
abstract class EssentialNonRootBinder[A: TypeShow, B: Decoder](
  convertFromString: BindingConfig ?=> String => A,
  convertFromExtracted: BindingConfig ?=> B => A,
  invalidMessageFunction: BindingLanguageConfig => (NestedFieldKey, String) => MessageParts
) extends TypedBinder[A]:

  /** Provides handlers for potential exceptions during the application of conversion/extraction functions.
    *
    * [[java.util.NoSuchElementException]] is handled by default even if no handler is provided by returning a
    * [[BindingFailure]] with a
    * [[com.magaran.svalidator.binding.config.BindingLanguageConfig.noValueProvidedMessage BindingLanguageConfig.noValueProvided]]
    * as the message.
    *
    * @param fieldKey The key that was being bound at the time of the exception
    * @param fieldValue The value that was being used, in string form, at the time of the binding
    * @param source The source that was providing the values for the binding
    * @param config The config used at the time of the binding
    * @param context The binding context information passed by the call site of the binding
    * @return The binding result according to the expected exception
    */
  def exceptionHandler(
    fieldKey: NestedFieldKey,
    fieldValue: String
  )(using Source, BindingConfig, BindingContext): PartialFunction[Throwable, BindingResult[A]]

  override def bind(source: Source, fieldKey: FieldKey)(using BindingConfig, BindingContext): BindingResult[A] =
    BinderUtils.nonRootBinding(source, fieldKey) { nestedFieldKey =>
      source match
        case valuesMap: ValuesMap   => bindValuesMap(valuesMap, nestedFieldKey)
        case jsonCursor: JsonCursor => bindJson(jsonCursor, nestedFieldKey)
    }

  private def manageExceptionsWithValueOption(fieldKey: NestedFieldKey, valueOption: Option[String])(
    block: => BindingResult[A]
  )(using Source, BindingConfig, BindingContext): BindingResult[A] =
    try
      try
        block
      catch
        exceptionHandler(
          fieldKey,
          valueOption
            .map(quoted)
            .getOrElse(quoted(""))
        )
    catch
      case ex: NoSuchElementException =>
        BindingFailure(fieldKey, summon[BindingLanguageConfig].noValueProvidedMessage(fieldKey), Some(ex))

  protected[typed] def beforeConvertFromStringHook: String => String = _.trim
  protected[typed] def postExtractIsValidFilter: B => Boolean        = _ => true

  private def quoted(str: String): String = StringBuilder().append('"').append(str).append('"').mkString

  private def bindValuesMap(source: ValuesMap, fieldKey: NestedFieldKey)(
    using BindingConfig,
    BindingContext
  ): BindingResult[A] =
    val valueOption = source.get(fieldKey).flatMap(_.headOption)
    manageExceptionsWithValueOption(fieldKey, valueOption) {
      BindingPass(convertFromString(beforeConvertFromStringHook(valueOption.filterNot(_.trim.isEmpty).get)))(
        using source
      )
    }(using source)

  private def bindJson(cursor: JsonCursor, fieldKey: NestedFieldKey)(
    using BindingConfig,
    BindingContext
  ): BindingResult[A] =
    val extracted = cursor.as[Option[B]]
    extracted match
      case Left(parsingFailure) =>
        val valueAtCursor = cursor.focus
          .map:
            case y if y.isString => y.asString.get
            case x               => x.toString
          .getOrElse("")
        BindingFailure(
          fieldKey,
          invalidMessageFunction(summon[BindingLanguageConfig])(fieldKey, quoted(valueAtCursor)),
          Some(parsingFailure)
        )(using cursor)
      case Right(value) =>
        manageExceptionsWithValueOption(fieldKey, value.map(_.toString())) {
          BindingPass(convertFromExtracted(value.filter(postExtractIsValidFilter).get))(using cursor)
        }(using cursor)
