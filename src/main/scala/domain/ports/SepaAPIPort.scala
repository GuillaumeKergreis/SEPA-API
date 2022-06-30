package domain.ports

import domain.SepaCreditTransferMessage
import generated.Document

abstract class SepaAPIPort[F[_]]() {
  def generate_credit_transfer_xml(sepaCreditTransferMessage: SepaCreditTransferMessage): F[Either[Throwable, Document]]

  def generate_credit_transfer_object(document: Document): F[Either[Throwable, SepaCreditTransferMessage]]
}
