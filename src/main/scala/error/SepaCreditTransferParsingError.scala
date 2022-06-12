package error


sealed trait SepaCreditTransferParsingError


object SepaCreditTransferParsingError {

  case class XmlParsingError(message: String) extends SepaCreditTransferParsingError
  case object MissingSepaSettlementDate extends SepaCreditTransferParsingError

}

