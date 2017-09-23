package com.andyr
import akka.http.javadsl.server.PathMatchers
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory

object ExtractPathServer extends HttpApp {
  override def routes: Route =
      path("order" / Segments) { x =>
          get { //curl -i -v -X GET http://0.0.0.0:8080/order/anything1/anything2
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Received Get Req with: $x"))
          } ~
            post { //curl -i -v -d "andy" -X POST http://0.0.0.0:8080/order/anything1/anything2
              entity(as[String]) { ent =>
                complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Received Post Req with $x and String: $ent "))
              }
            }
    }
}

object SimpleExtractPathServerApp extends App {
  ExtractPathServer.startServer("0.0.0.0", 8080, ServerSettings(ConfigFactory.load))
}