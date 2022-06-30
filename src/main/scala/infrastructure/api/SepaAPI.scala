package infrastructure.api

import cats.effect.IO
import cats.implicits._
import domain.SepaCreditTransferMessage
import domain.ports.SepaAPIPort
import generated.Document
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import sttp.tapir.Codec.XmlCodec
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir._
import cats.syntax._


import scala.util.Try

class SepaAPI(sepaAPIPort: SepaAPIPort[IO]) {

  implicit val xmlCodec: XmlCodec[Document] =
    Codec.xml[Document] {
      xmlString => DecodeResult.fromOption[Document](Try(scalaxb.fromXML[Document](xml.XML.loadString(xmlString))).toOption)
    } {
      xmlDocument => scalaxb.toXML[Document](xmlDocument, "Document", generated.defaultScope).toString()
    } {
      Schema.string[Document]
    }

  val creditTransferPath: Endpoint[Unit, Unit, Unit, Unit, Any] = endpoint.in("credit-transfer").tag("Credit transfer")

  val generateSepaFile =
    creditTransferPath.post.in("generate-sepa-file")
      .in(jsonBody[SepaCreditTransferMessage])
      .out(xmlBody[Document])
      .serverLogic[IO](sepaCreditTransferMessage => {
        sepaAPIPort.generate_credit_transfer_xml(sepaCreditTransferMessage).map(_.leftMap(_ => ()))
      })

  val decodeSepaFile =
    creditTransferPath.post.in("decode-sepa-file")
      .in(xmlBody[Document])
      .in(query[String]("test"))
      .out(jsonBody[SepaCreditTransferMessage])
      .serverLogic[IO]{ case (document, test) =>
        sepaAPIPort.generate_credit_transfer_object(document).map(_.leftMap(e => {
          println(e)
          ()
        }))
      }

  val enpoints = List(generateSepaFile, decodeSepaFile)

  val swaggerEnpoint = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.pathPrefix(List("swagger")))
    .fromServerEndpoints(enpoints, "SEPA Credit Transfer API", "1.0")

  val routes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(enpoints ++ swaggerEnpoint)

}
