package com.andyr
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
object SimpleMarshallingServer extends HttpApp with SharePricesMarshaller {
  var price: Option[SharePrices] = None
  override def routes: Route =
    get { // curl -i -v -X GET http://0.0.0.0:8080
      complete {
        price match {
          case None => {
            StatusCodes.NotFound -> "No Price Stored"
          }
          case Some(value) => StatusCodes.OK -> value
        }
      }
    } ~
      post { //curl -i -v -d "AAPL,100,33.45,33.55,33.25,0.01" -X POST http://0.0.0.0:8080
        entity(as[SharePrices]) { shareprice =>
          complete {
            price = Some(shareprice)
            StatusCodes.OK -> s"Price is $price"
          }
        }
      }
}


object SimpleMarshallingServerApp extends App {
  SimpleMarshallingServer.startServer("0.0.0.0", 8080, ServerSettings(ConfigFactory.load))
}
