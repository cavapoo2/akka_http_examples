package com.andyr
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

object BitcoinPrice extends App {

  def inputCheck(args: Array[String]): Option[String] = {
    val currencies:List[String] = List("USD","AUD","BRL","CAD","CHF","CLP","CNY","DKK","EUR","GBP","HKD","INR","ISK","JPY",
      "KRW","NZD","PLN","RUB","SEK","SGD","THB","TWD")
    if (args.length != 1) {
      println("Need to supply the currency from this list")
      println(currencies)
      return None
    }
    val curr = args(0)
    if (currencies.contains(curr))
      return Some(curr)
    else
      return None
  }
  def getJson(curr:String): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val flow = Http().outgoingConnectionHttps("blockchain.info")
    val req = HttpRequest(uri = "/ticker")

    def filter(data:String,curr:String  ):Option[String] = {
      val lines = data.split("\n")
      val res = for {
        line <- lines
        if (line.contains(curr))
      } yield line
      res.headOption
    }
    val responseFuture = Source.single(req).via(flow).runWith(Sink.head)
    responseFuture.andThen {
      case Success(response) =>
        response.entity.toStrict(5 seconds).map(_.data.decodeString("UTF-8")).andThen {
          case Success(json) =>
            val data = filter(json, curr)
            val jdata = data match {
              case Some(a) => println(a)
              case _ => println("Error 2")
            }
            materializer.shutdown()
            system.terminate()
          case _ =>
            println("Error 1")
        }
      case _ => println("request failed");
    }
  }
  //run the code
  inputCheck(args).map(getJson(_))


}
