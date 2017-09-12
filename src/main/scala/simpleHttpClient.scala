package com.andyr
import akka.actor.ActorSystem
import akka.http.scaladsl.{Http}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object SimpleHttpClientApp extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //prepare requests
  val baseUrl = "http://0.0.0.0:8080/simple/num/"
  val data = Seq(("GET", "1", ""), ("GET", "2", ""), ("POST", "1", "Akka"), ("POST", "2", "Scala"))
  val reqs = data.map { case (a, b, c) =>
    if (a == "GET")
      HttpRequest(GET, Uri(baseUrl + b), Nil)
    else
      HttpRequest(POST, Uri(baseUrl + b), Nil, HttpEntity(ContentTypes.`text/plain(UTF-8)`, ByteString(c)))
  }
  //send off the requests to the server
  Future.traverse(reqs)(Http().singleRequest(_)) andThen {
    case Success(resps) => resps.foreach(resp =>
      resp.entity.toStrict(5 seconds).map(_.data.utf8String).andThen{
        case Success(content) => println(s"Response: $content")
        case _ => println("Error")
      })
    case Failure(err) => println(s"Request failed $err")
  }
}
