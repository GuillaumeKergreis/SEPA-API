package model

import util.{Bic, Iban}

case class SepaCreditTransferTransaction(
                                          instructionId: Option[String],
                                          endToEndId: String,
                                          sepaTxId: String,
                                          amount: BigDecimal,
                                          debtorName: String,
                                          debtorAccount: Iban,
                                          debtorAgent: Bic,
                                          creditorName: String,
                                          creditorAccount: Iban,
                                          creditorAgent: Bic,
                                          purposeCode: Option[String],
                                          description: Option[String]
                                        )
