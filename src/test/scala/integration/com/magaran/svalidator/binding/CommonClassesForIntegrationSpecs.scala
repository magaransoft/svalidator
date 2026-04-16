package integration.com.magaran.svalidator.binding

import java.time.LocalDate
import java.util.UUID

import com.magaran.svalidator.binding.binders.TypedBinder
import integration.com.magaran.svalidator.binding

enum AScala3Enum(val id: Int, val description: String, val somethingElse: Any) derives TypedBinder:
  case ScalaBasedFirstOption  extends AScala3Enum(1, "The first scala option", "anything4")
  case ScalaBasedSecondOption extends AScala3Enum(2, "The second scala option", BigDecimal("390"))
  case ScalaBasedThirdOption  extends AScala3Enum(3, "The third scala option", true)

trait SomeTrait:
  def someMethod: String

case class AClassWithAGenericField[A](aGenericField: A) derives TypedBinder

case class AComplexClass(
  aString: String,
  anInt: Int,
  aLong: Long,
  aBoolean: Boolean,
  aLocalDate: LocalDate,
  optionalText: Option[String],
  optionalInt: Option[Int],
  intList: List[Int],
  longArray: Array[Long],
  setOfStrings: Set[String],
  vectorOfFloats: Vector[Float],
  aTypeBasedEnum: AScala3Enum,
  anUUID: UUID,
  aClassWithAGenericField: AClassWithAGenericField[Long]
) derives TypedBinder:
  override def equals(other: Any): Boolean =
    other match
      case value: AComplexClass =>
        aString == value.aString &&
        anInt == value.anInt &&
        aLong == value.aLong &&
        aBoolean == value.aBoolean &&
        aLocalDate == value.aLocalDate &&
        optionalText == value.optionalText &&
        optionalInt == value.optionalInt &&
        intList == value.intList &&
        longArray.sameElements(value.longArray) &&
        setOfStrings == value.setOfStrings &&
        vectorOfFloats == value.vectorOfFloats &&
        aTypeBasedEnum == value.aTypeBasedEnum &&
        anUUID == value.anUUID
      case _ => false

  override def hashCode(): Int = aString.hashCode()

case class AClassWithAnIndexedList(anIndexedList: List[AnIndexedListValue]) derives TypedBinder

case class AnIndexedListValue(stringField: String, longField: Long) derives TypedBinder
