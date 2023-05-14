package forex.API.model

import java.time.OffsetDateTime

case class ErrorResponse(error: String)

object ErrorResponse {
  implicit def decoder: io.circe.Decoder[ErrorResponse] = io.circe.Decoder.forProduct1("error")(ErrorResponse.apply)
  implicit def encoder: io.circe.Encoder[ErrorResponse] = io.circe.Encoder.forProduct1("error")(errResp => (errResp.error))
}

case class CurrencyRate(from: String,
                        to: String,
                        bid: Double,
                        ask: Double,
                        price:Double,
                        time_stamp:OffsetDateTime)

object CurrencyRate {
  implicit def decoder: io.circe.Decoder[CurrencyRate] = io.circe.Decoder.forProduct6("from", "to", "bid", "ask", "price", "time_stamp")(CurrencyRate.apply)
  implicit def encoder: io.circe.Encoder[CurrencyRate] = io.circe.Encoder.forProduct6("from", "to", "bid", "ask", "price", "time_stamp")(rate => (rate.from, rate.to, rate.bid, rate.ask, rate.price, rate.time_stamp))
}
