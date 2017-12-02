package io.github.tjheslin1

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.github.tjheslin1.dogs.{Dog, Medium}
import io.github.tjheslin1.error.ParseError

import scala.concurrent.Future

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterialize≠r()
  implicit val executionContext = system.dispatcher

  val dog = Dog("Woody", "black", Medium)

  println(dog.asJson.noSpaces)

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    receiveDog orElse {
      case req: HttpRequest =>
        req.discardEntityBytes()
        Future(HttpResponse(404, entity = "Unknown resource!"))
    }
  }

  def receiveDog = {
    handlePath(POST, "/dog",
      HttpEntity(ContentTypes.`application/json`, dog.asJson.noSpaces))
  }

  private def handlePath(method: HttpMethod, path: String, resp: ResponseEntity): PartialFunction[HttpRequest, Future[HttpResponse]] = {
    case HttpRequest(`method`, Uri.Path(`path`), _, reqEntity, _) =>

      val parsedDog: Either[ParseError, Dog] = for {
        json <- parse(reqEntity.dataBytes.toString()).leftMap(err => ParseError(reqEntity.dataBytes.toString, err.message))
        dog <- json.as[Dog].leftMap(err => ParseError(json.noSpaces, err.message))
      } yield dog

      parsedDog match {
        case Right(dog) => Future(HttpResponse(entity = s"""{ "created": ${dog.asJson.noSpaces}}""", status = StatusCodes.Created))
        case Left(parseErr) => Future(HttpResponse(entity = s"""{ "error": ${parseErr.asJson.noSpaces}}""", status = StatusCodes.BadRequest))
      }
//      parsedDog.map(dog => Future(HttpResponse(entity = s"""{ "created": ${dog.asJson.noSpaces}}""", status = StatusCodes.Created)))
//          .getOrElse(parseError => Future(HttpResponse(entity = s"""{ "error": ${parseError.asJson.noSpaces}}""", status = StatusCodes.BadRequest)))
  }

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = "localhost", port = 8080)

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach { connection =>
      connection handleWithAsyncHandler requestHandler
    }).run()
}
