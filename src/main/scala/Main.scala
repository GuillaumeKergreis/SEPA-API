import cats.effect._
import cats.implicits.{catsSyntaxEitherId, toBifunctorOps}
import generated.Document
import io.circe.generic.auto._
import model.{SepaCreditTransferMessage, SepaCreditTransferTransaction}
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import sttp.model.StatusCode
import sttp.tapir.Codec.XmlCodec
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.{Codec, DecodeResult, Endpoint, Schema, endpoint, statusCode, xmlBody}
import util.{Bic, Iban}

import java.time.{LocalDate, OffsetDateTime}
import scala.util.Try


object Main extends IOApp {

  implicit val xmlCodec: XmlCodec[Document] =
    Codec.xml[Document] {
      xmlString => DecodeResult.fromOption[Document](Try(scalaxb.fromXML[Document](xml.XML.loadString(xmlString))).toOption)
    } {
      xmlDocument => scalaxb.toXML[Document](xmlDocument, "Document", generated.defaultScope).toString()
    } {
      Schema.string[Document]
    }

  val creditTransferPath: Endpoint[Unit, Unit, Unit, Any] = endpoint.in("credit-transfer").tag("Credit transfer")

  val generateSepaFile: ServerEndpoint[SepaCreditTransferMessage, Unit, Document, Any, IO] =
    creditTransferPath.post.in("generate-sepa-file")
      .in(jsonBody[SepaCreditTransferMessage])
      .out(xmlBody[Document])
      .serverLogic(sepaCreditTransferMessage => {
        println(sepaCreditTransferMessage.toDocument)
        IO.pure(sepaCreditTransferMessage.toDocument.asRight[Unit])
      })

  val decodeSepaFile: ServerEndpoint[Document, ErrorOutput, SepaCreditTransferMessage, Any, IO] =
    creditTransferPath.post.in("decode-sepa-file")
      .in(xmlBody[Document])
      .out(jsonBody[SepaCreditTransferMessage])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorOutput]))
      .serverLogic(document => {
        println(document)
        IO.pure(SepaCreditTransferMessage.fromDocument(document).toEither.leftMap(e => ErrorOutput(e.getMessage)))
      })

  val enpoints = List(generateSepaFile, decodeSepaFile)
  val routes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(enpoints)

  val docs: OpenAPI = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(enpoints, "SEPA Credit Transfer API", "1.0")
  val docRoute: HttpRoutes[IO] = new SwaggerHttp4s(docs.toYaml, contextPath = List("swagger")).routes

  def run(args: List[String]): IO[ExitCode] = {
    val host = "localhost"
    val port = 8080
    println(s"Development server started on http://$host:$port")
    println(s"Swagger is available on http://$host:$port/swagger")

    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(Router("/" -> routes, "/" -> docRoute).orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  case class ErrorOutput(error: String)
}