package com.andyr
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._

trait SharePricesMarshaller {
  val contentType = ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`)
  implicit val utf8TextSpaceMarshaller: ToEntityMarshaller[SharePrices] =
    Marshaller.withFixedContentType(contentType) { shareprice ⇒ HttpEntity(contentType, shareprice.marshall) }
  implicit val utf8TextSpaceUnmarshaller: FromEntityUnmarshaller[SharePrices] =
    Unmarshaller.stringUnmarshaller.map(SharePrices.unmarshall)
}
case class SharePrices(ticker: String, quantity: Long, spot: Double, bid: Double, offer: Double, vol: Double) {
  val marshall = s"$ticker,$quantity,$spot,$bid,$offer,$vol"
}

object SharePrices {
  def unmarshall(str: String) = {
    str.split(",") match {
      case Array(t,q,s,b,o,v) =>
        SharePrices(t,q.toLong,s.toDouble,b.toDouble,o.toDouble,v.toDouble)
    }
  }
}
