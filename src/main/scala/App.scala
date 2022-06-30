import cats.effect._
import domain.SepaCreditTransferProcessor
import doobie.util.transactor.Transactor
import infrastructure.api.SepaAPI
import infrastructure.database.SepaAPIDatabase
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router


case class CatNinjaAPIOutput(fact: String, length: Int)

object App extends IOApp {

  val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/sepa_api",
    user = "postgres",
    pass = "password"
  )

  val database = new SepaAPIDatabase(xa)

  val sepaCreditTransferProcessor = new SepaCreditTransferProcessor(database)

  val sepaAPI = new SepaAPI(sepaCreditTransferProcessor)


  def run(args: List[String]): IO[ExitCode] = {
    val host = "localhost"
    val port = 8080
    println(s"Development server started on http://$host:$port")
    println(s"Swagger is available on http://$host:$port/swagger")


    //
    //    val catNinjaAPIUri = uri"https://catfact.ninja/fact"
    //    val catNinjaRequest = Request[IO](uri = catNinjaAPIUri)

    for {
      //      _ <- BlazeClientBuilder[IO].resource.use(client =>
      //        client.expect[CatNinjaAPIOutput](catNinjaRequest)
      //          .map(res => println(res.fact)))

      res <- BlazeServerBuilder[IO]
        .bindHttp(port, host)
        .withHttpApp(Router("/" -> sepaAPI.routes).orNotFound)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield res

  }

  case class ErrorOutput(error: String)
}