package forex.programs.rates

import cats.effect.{ContextShift, IO, Timer}
import forex.API.OneFrameAPI

import java.time.OffsetDateTime
import scala.collection.mutable
import scala.concurrent.ExecutionContext.global

object CurrencyRateCache {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private val oneFrameAPI = new OneFrameAPI[IO]

  private val rates: mutable.Map[(String, String), (Double, OffsetDateTime)] = mutable.Map.empty

  def getRate(from: String, to: String): Option[Double] = {
    rates.get((from, to)).map {
      case (rate, lastUpdate) =>
        if (lastUpdate.isBefore(OffsetDateTime.now.minusMinutes(5))) {
          // The rate is older than 5 minutes, so it needs to be updated
          updateRate(from, to)
        }
        rate
    }
  }

  private def updateRate(from: String, to: String): Double = {

    val rateList = Set(s"$from$to")
    // Call the 3rd party API to get the latest rate
    val rateResponse = oneFrameAPI.getRate(rateList)

    // Check if the response is valid
    rateResponse match {
      case Some(rate) =>
        // Update the cache with the new rate and timestamp
        rates((from, to)) = (rate, OffsetDateTime.now)
        rate
      case None =>
        // The rate does not exist, so remove it from the cache
        rates.remove((from, to))
        throw new IllegalArgumentException(s"Invalid currency pair: $from$to")
    }
  }
}
