package io.gen4s.outputs.avro

import java.time.format.DateTimeFormatter

import org.apache.avro.Schema

import scala.annotation.nowarn

import cats.implicits.*
import io.github.agolovenko.avro.json.JsonParser
import io.github.agolovenko.avro.StringParsers.*

import play.api.libs.json.Json
import vulcan.{Avro, AvroError, Codec}
import vulcan.Codec.Aux

object AvroCodec {

  private val parsers =
    primitiveParsers orElse base64Parsers orElse zonedDateTimeParsers(DateTimeFormatter.ISO_ZONED_DATE_TIME)

  @nowarn
  def keyCodec(recordSchema: Schema): Aux[Avro.Record, AvroDynamicKey] = {
    val converter = new JsonParser(
      recordSchema,
      parsers
    )

    def encoder(p: AvroDynamicKey): Either[AvroError, Avro.Record] = {
      Either
        .catchNonFatal(converter.apply(Json.parse(Option(p).getOrElse(AvroDynamicKey.empty).bytes)))
        .leftMap(ex => AvroError.apply(s"Avro key encoder error: ${ex.getMessage}"))
    }

    def decoder(rec: Any, schema: Schema): Either[AvroError, AvroDynamicKey] =
      AvroDynamicKey(Array.emptyByteArray).asRight[AvroError]

    Codec.instance[Avro.Record, AvroDynamicKey](
      schema = recordSchema.asRight[AvroError],
      encode = encoder,
      decode = decoder
    )
  }

  @nowarn
  def valueCodec(recordSchema: Schema): Aux[Avro.Record, AvroDynamicValue] = {
    val converter = new JsonParser(
      recordSchema,
      parsers
    )

    def encoder(v: AvroDynamicValue): Either[AvroError, Avro.Record] = {
      Either
        .catchNonFatal(converter.apply(Json.parse(v.bytes)))
        .leftMap(ex => AvroError.apply(s"Avro value encoder error: ${ex.getMessage}"))
    }

    def decoder(rec: Any, schema: Schema): Either[AvroError, AvroDynamicValue] =
      AvroDynamicValue(Array.emptyByteArray).asRight[AvroError]

    Codec.instance[Avro.Record, AvroDynamicValue](
      schema = recordSchema.asRight[AvroError],
      encode = encoder,
      decode = decoder
    )
  }

}
