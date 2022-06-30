package infrastructure.database

import cats.effect._
import domain.SepaCreditTransferTransaction
import domain.ports.SepaPersistencePort
import doobie.Put
import doobie.Get
import doobie.WeakAsync.doobieWeakAsyncForAsync
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.implicits.javasql._

import java.sql.Timestamp
import java.time.OffsetDateTime

class SepaAPIDatabase(xa: Transactor[IO]) extends SepaPersistencePort[IO] {
  override def saveCreditTransferTransaction(sepaCreditTransferTransaction: SepaCreditTransferTransaction): IO[SepaCreditTransferTransaction] = {

    val request =
      sql"""INSERT INTO sepa_credit_transfer_transaction values (
                                                    ${sepaCreditTransferTransaction.instructionId},
                                                    ${sepaCreditTransferTransaction.endToEndId},
                                                    ${sepaCreditTransferTransaction.sepaTxId},
                                                    ${sepaCreditTransferTransaction.amount},
                                                    ${sepaCreditTransferTransaction.debtorName},
                                                    ${sepaCreditTransferTransaction.debtorAccount.iban},
                                                    ${sepaCreditTransferTransaction.debtorAgent.bic},
                                                    ${sepaCreditTransferTransaction.creditorName},
                                                    ${sepaCreditTransferTransaction.creditorAccount.iban},
                                                    ${sepaCreditTransferTransaction.creditorAgent.bic},
                                                    ${sepaCreditTransferTransaction.purposeCode},
                                                    ${sepaCreditTransferTransaction.description}
                                                    )"""
    request.update.run.transact[IO](xa).map(_ => sepaCreditTransferTransaction)
      .handleError(e => {println(e)
        sepaCreditTransferTransaction
      })
  }

  override def getCreditTransferTransaction(sepaTxId: String): IO[Option[SepaCreditTransferTransaction]] = {
    val request =
      sql"""SELECT instruction_id,
                  end_to_end_id,
                  sepa_tx_id,
                  amount,
                  debtor_name,
                  debtor_account,
                  debtor_agent,
                  creditor_name,
                  creditor_account,
                  creditor_agent,
                  purpose_code,
                  description
                  FROM sepa_credit_transfer_transaction WHERE sepa_tx_id=$sepaTxId"""

    request.query[SepaCreditTransferTransaction].option.transact(xa)
  }
}
