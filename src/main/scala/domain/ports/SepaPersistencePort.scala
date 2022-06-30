package domain.ports

import domain.SepaCreditTransferTransaction

abstract class SepaPersistencePort[F[_]]() {
  def saveCreditTransferTransaction(sepaCreditTransferTransaction: SepaCreditTransferTransaction): F[SepaCreditTransferTransaction]

  def getCreditTransferTransaction(sepaTxId: String): F[Option[SepaCreditTransferTransaction]]
}
