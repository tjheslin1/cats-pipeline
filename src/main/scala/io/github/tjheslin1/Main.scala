package io.github.tjheslin1

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val requestHandler: HttpRequest => HttpResponse = {
    defaultPath orElse
      handlePing orElse {

      case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
        sys.error("BOOM!")

      case r: HttpRequest =>
        r.discardEntityBytes()
        HttpResponse(404, entity = "Unknown resource!")
    }
  }

  def defaultPath: PartialFunction[HttpRequest, HttpResponse] = {
    handlePath(GET, "/",
      HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
  }

  def handlePing: PartialFunction[HttpRequest, HttpResponse] = {
    handlePath(GET, "/ping", "PONG!")
  }

  private def handlePath(method: HttpMethod, path: String, resp: ResponseEntity): PartialFunction[HttpRequest, HttpResponse] = {
    case HttpRequest(`method`, Uri.Path(`path`), _, _, _) =>
      HttpResponse(entity = resp)
  }

  val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
