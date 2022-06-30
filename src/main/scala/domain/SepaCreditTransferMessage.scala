package domain

import generated.{AccountIdentification4Choice, ActiveCurrencyAndAmount, BranchAndFinancialInstitutionIdentification4, CLRG, CashAccount16, CreditTransferTransactionInformation11, Document, FIToFICustomerCreditTransferV02, FinancialInstitutionIdentification7, GroupHeader33, PartyIdentification32, PaymentIdentification3, PaymentTypeInformation21, Purpose2Choice, RemittanceInformation5, SLEV, ServiceLevel8Choice, SettlementInformation13, defaultScope}
import scalaxb.DataRecord
import scalaxb.DataRecord.__StringXMLFormat
import util.{Bic, Iban}

import java.time.{LocalDate, OffsetDateTime}
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq


case class SepaCreditTransferMessage(
                                      sepaMessageId: String,
                                      sepaCreationDateTime: OffsetDateTime,
                                      sepaSettlementDate: LocalDate,
                                      processingDateTime: Option[OffsetDateTime],
                                      instigatingAgent: Option[Bic],
                                      instigatedAgent: Option[Bic],
                                      sepaCreditTransferTransactions: List[SepaCreditTransferTransaction]
                                    ) {
  def toDocument: Document = Document(
    FIToFICustomerCreditTransferV02(
      GrpHdr = GroupHeader33(
        MsgId = sepaMessageId,
        CreDtTm = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(sepaCreationDateTime.toZonedDateTime)),
        NbOfTxs = sepaCreditTransferTransactions.length.toString,
        TtlIntrBkSttlmAmt = Some(ActiveCurrencyAndAmount(value = sepaCreditTransferTransactions.map(_.amount).sum, Map(("@Ccy", DataRecord("EUR"))))),
        IntrBkSttlmDt = Some({
          val IntrBkSttlmDt = DatatypeFactory.newInstance().newXMLGregorianCalendar
          IntrBkSttlmDt.setYear(sepaSettlementDate.getYear)
          IntrBkSttlmDt.setMonth(sepaSettlementDate.getMonthValue)
          IntrBkSttlmDt.setDay(sepaSettlementDate.getDayOfMonth)
          IntrBkSttlmDt
        }),
        SttlmInf = SettlementInformation13(SttlmMtd = CLRG),
        PmtTpInf = Some(PaymentTypeInformation21(SvcLvl = Some(ServiceLevel8Choice(DataRecord(<Cd></Cd>, "SEPA"))))),
        InstgAgt = instigatingAgent.map(agentBic => BranchAndFinancialInstitutionIdentification4(FinancialInstitutionIdentification7(Some(agentBic.bic)))),
        InstdAgt = instigatedAgent.map(agentBic => BranchAndFinancialInstitutionIdentification4(FinancialInstitutionIdentification7(Some(agentBic.bic)))),
      ),
      CdtTrfTxInf = sepaCreditTransferTransactions.map(transaction => CreditTransferTransactionInformation11(
        PmtId = PaymentIdentification3(
          InstrId = transaction.instructionId,
          EndToEndId = transaction.endToEndId,
          TxId = transaction.sepaTxId,
        ),
        IntrBkSttlmAmt = ActiveCurrencyAndAmount(value = transaction.amount, Map(("@Ccy", DataRecord("EUR")))),
        ChrgBr = SLEV,
        Dbtr = PartyIdentification32(Nm = Some(transaction.debtorName)),
        DbtrAcct = Some(CashAccount16(Id = AccountIdentification4Choice(DataRecord(<IBAN></IBAN>, transaction.debtorAccount.iban)))),
        DbtrAgt = BranchAndFinancialInstitutionIdentification4(FinInstnId = FinancialInstitutionIdentification7(BIC = Some(transaction.debtorAgent.bic))),
        Cdtr = PartyIdentification32(Nm = Some(transaction.creditorName)),
        CdtrAcct = Some(CashAccount16(Id = AccountIdentification4Choice(DataRecord(<IBAN></IBAN>, transaction.creditorAccount.iban)))),
        CdtrAgt = BranchAndFinancialInstitutionIdentification4(FinInstnId = FinancialInstitutionIdentification7(BIC = Some(transaction.creditorAgent.bic))),
        Purp = transaction.purposeCode.map(purposeCode => Purpose2Choice(DataRecord(<Cd></Cd>, purposeCode))),
        RmtInf = transaction.description.map(description => RemittanceInformation5(Ustrd = Seq(description)))
      ))
    )
  )

  def toXml: NodeSeq = scalaxb.toXML[Document](toDocument, "Document", defaultScope)
}

case object SepaCreditTransferMessage {
  def fromXml(xmlFile: NodeSeq): Try[SepaCreditTransferMessage] =
    Try(scalaxb.fromXML[Document](xmlFile)).flatMap(document => fromDocument(document))

  def fromDocument(document: Document): Try[SepaCreditTransferMessage] = {
    for {
      sepaSettlementDate <- document.FIToFICstmrCdtTrf.GrpHdr.IntrBkSttlmDt match {
        case Some(value) => Success(value.toGregorianCalendar.toZonedDateTime.toLocalDate)
        case None => Failure(new Exception("Missing interbank settlement date"))
      }
    } yield SepaCreditTransferMessage(
      sepaMessageId = document.FIToFICstmrCdtTrf.GrpHdr.MsgId,
      sepaCreationDateTime = document.FIToFICstmrCdtTrf.GrpHdr.CreDtTm.toGregorianCalendar.toZonedDateTime.toOffsetDateTime,
      sepaSettlementDate = sepaSettlementDate,
      processingDateTime = None,
      instigatingAgent = document.FIToFICstmrCdtTrf.GrpHdr.InstgAgt.flatMap(_.FinInstnId.BIC.map(Bic)),
      instigatedAgent = document.FIToFICstmrCdtTrf.GrpHdr.InstdAgt.flatMap(_.FinInstnId.BIC.map(Bic)),
      sepaCreditTransferTransactions = document.FIToFICstmrCdtTrf.CdtTrfTxInf.map(transaction =>
        domain.SepaCreditTransferTransaction(
          instructionId = transaction.PmtId.InstrId,
          endToEndId = transaction.PmtId.EndToEndId,
          sepaTxId = transaction.PmtId.TxId,
          amount = transaction.IntrBkSttlmAmt.value,
          debtorName = transaction.Dbtr.Nm.getOrElse(""),
          debtorAccount = transaction.DbtrAcct.map(_.Id.accountidentification4choicableoption.value.toString).map(Iban).getOrElse(Iban("")),
          debtorAgent = transaction.DbtrAgt.FinInstnId.BIC.map(Bic).getOrElse(Bic("")),
          creditorName = transaction.Cdtr.Nm.getOrElse(""),
          creditorAccount = transaction.CdtrAcct.map(_.Id.accountidentification4choicableoption.value.toString).map(Iban).getOrElse(Iban("")),
          creditorAgent = transaction.CdtrAgt.FinInstnId.BIC.map(Bic).getOrElse(Bic("")),
          purposeCode = transaction.Purp.map(_.purpose2choicableoption.value),
          description = transaction.RmtInf.flatMap(_.Ustrd.headOption)
        )
      ).toList
    )
  }
}


