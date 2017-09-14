package com.andyr
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class Message(message: String, contents: String="")

object SimpleMarshallingClientApp extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  //prepare requests
  val baseUrl = "http://0.0.0.0:8080"
  val data = Seq(Message("POST","AAPL,100,33.45,33.55,33.25,0.01"), Message("GET"),Message("POST","AMZN,1000,133.45,133.55,133.25,0.05"),Message("GET"))
  val reqs = data.map {
    case Message(a,_) if(a == "GET") =>
      println("send get")
      HttpRequest(GET, Uri(baseUrl), Nil)
    case Message(a,b) =>
      println("send post")
      HttpRequest(POST, Uri(baseUrl), Nil, HttpEntity(ContentTypes.`text/csv(UTF-8)`, ByteString(b)))
  }
  //a way to send requests in a sequence recursively, in order, and only send next if previous was successful.
  //Also there is another way below (sendRequests2 which is more inline with the Stream style of coding.
  def sendRequests(reqs : List[HttpRequest]): Unit = reqs match {
    case h :: t => Http().singleRequest(h) andThen {
      case Success(resp) => resp.entity.toStrict(5 seconds).map(_.data.utf8String).andThen {
        case Success(content) =>
          println(s"Response: $content")
          sendRequests(t)
        case _ => println("Error")
      }
    }
    case Nil => println("All Done")
  }
  //this traverse can be used if you don't care about order of sending messages
/*
  //send off the requests to the server
  Future.traverse(reqs)(Http().singleRequest(_)) andThen {
    case Success(resps) => resps.foreach(resp =>
      resp.entity.toStrict(5 seconds).map(_.data.utf8String).andThen{
        case Success(content) => println(s"Response: $content")
        case _ => println("Error")
      })
    case Failure(err) => println(s"Request failed $err")
  }
*/
  //send the messages stream style.
  def sendRequests2(reqs : List[HttpRequest]): Unit = {
    Source(reqs)
     .mapAsync(1){
       req =>
         for {
           resp <- Http().singleRequest(req)
           content <- resp.entity.toStrict(5 seconds).map(_.data.utf8String)
         } yield content
      }
     .runWith(Sink.foreach(content => println(s"Response: $content")))
     .foreach(_ => println("All Done"))
  }
  sendRequests2(reqs.toList)
}
