package forex.API

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import forex.API.model.{CurrencyRate, ErrorResponse}
import org.http4s.client.blaze.BlazeClientBuilder
import forex.config._
import org.http4s._
import org.http4s.EntityDecoder
import io.circe.{Decoder, parser}
import io.circe.generic.semiauto.deriveDecoder

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext.global

class OneFrameAPI[F[_]: ConcurrentEffect: Timer] {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  implicit val currencyRateDecoder: Decoder[CurrencyRate] = deriveDecoder[CurrencyRate]
  implicit val errorResponseDecoder: Decoder[ErrorResponse] = deriveDecoder[ErrorResponse]

  private val client = BlazeClientBuilder[IO](global).resource

  def getRate(pairs: Set[String]): IO[Map[String,(Double, OffsetDateTime)]] = {
    var rates: Map[String, (Double, OffsetDateTime)] = Map.empty

    for{
      config <- Config.stream("app")
      pairQueryParams = pairs.map(p => (config.oneFrame.query, p)).toMap
      uri = Uri.unsafeFromString(config.oneFrame.host)
        .withPath(config.oneFrame.path)
        .withQueryParams(pairQueryParams)
      resp <- client.use(_.expect[String](uri)(EntityDecoder.text[IO]))
      json <- IO.fromEither(parser.parse(resp))
      result <- json.as[Either[ErrorResponse, List[CurrencyRate]]] match {
        case Right(rateList) =>
          rateList.map(rate => rate.map(r => {
            val pair = s"${r.from}${r.to}"
            rates.updated(pair,(r, r.time_stamp))
          }))
          val updatedRates = rateList.map(rate => rate.map(r =>{
            val pair = s"${r.from}${r.to}"
            pair -> (r.price, r.time_stamp)
          }).toMap)
          rates = rates ++ updatedRates
          IO(rates)
        case Left(err) =>
          IO.raiseError(new Exception(err.message))
        case _ =>
          IO.raiseError(new Exception("Unexpected API response"))
      }
    }yield rates
  }
}