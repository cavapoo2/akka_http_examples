package com.andyr
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{HttpApp,Route}
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory

object SimpleHttpServer extends HttpApp {
  override def routes: Route =
    pathPrefix("simple") {
      path("num" / IntNumber) { num =>
        get {  //curl -i -v -X GET http://0.0.0.0:8080/simple/num/1
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Received Get Req with num: $num"))
        } ~
          post { //curl -i -v -d "andy" -X POST http://0.0.0.0:8080/simple/num/1
            entity(as[String]) { ent =>
              complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Received Post Req with num: $num and String: $ent "))
            }
          }
      }
    }
}

object SimpleHttpServerApp extends App {
  SimpleHttpServer.startServer("0.0.0.0", 8080, ServerSettings(ConfigFactory.load))
}
