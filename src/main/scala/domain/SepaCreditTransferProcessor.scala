package domain

import cats.effect.IO
import cats.syntax.all._
import domain.ports.{SepaAPIPort, SepaPersistencePort}
import generated.Document

class SepaCreditTransferProcessor(sepaPersistencePort: SepaPersistencePort[IO]) extends SepaAPIPort[IO] {
  override def generate_credit_transfer_xml(sepaCreditTransferMessage: SepaCreditTransferMessage): IO[Either[Throwable, Document]] =
    IO(Right(sepaCreditTransferMessage.toDocument))

  override def generate_credit_transfer_object(document: Document): IO[Either[Throwable, SepaCreditTransferMessage]] = {

    for {
      sepaCreditTransferMessage <- IO(SepaCreditTransferMessage.fromDocument(document).toEither)
      _ <- IO.fromEither(sepaCreditTransferMessage).flatMap(_.sepaCreditTransferTransactions.traverseTap(sepaPersistencePort.saveCreditTransferTransaction))
    } yield sepaCreditTransferMessage

  }

}
