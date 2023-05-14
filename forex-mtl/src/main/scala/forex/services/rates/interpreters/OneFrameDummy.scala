package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.programs.rates.CurrencyRateCache
import forex.services.rates.errors._

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val rate = CurrencyRateCache.getRate(pair.from.toString, pair.to.toString)
    Rate(pair, Price(BigDecimal(rate.get)), Timestamp.now).asRight[Error].pure[F]
  }

}
