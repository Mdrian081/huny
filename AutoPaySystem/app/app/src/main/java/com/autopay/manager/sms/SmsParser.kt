package com.autopay.manager.sms

data class ParsedPayment(
    val method: String,
    val type: String,       // Personal / Merchant / Agent
    val amount: Double,
    val trxId: String
)

/**
 * Very small, dependency-free parser for the two most common Bangladeshi
 * MFS (mobile financial service) incoming-payment SMS formats.
 * Extend the regex list below if your account receives other message shapes.
 */
object SmsParser {

    private val nagadCashIn = Regex(
        """received\s*Tk\.?\s*([\d,]+\.?\d*).*?TxnID:?\s*([A-Za-z0-9]+)""",
        RegexOption.IGNORE_CASE
    )

    private val bkashReceived = Regex(
        """received\s*Tk\s*([\d,]+\.?\d*)\s*from.*?TrxID\s*([A-Za-z0-9]+)""",
        RegexOption.IGNORE_CASE
    )

    fun parse(sender: String, body: String): ParsedPayment? {
        val lowerSender = sender.lowercase()

        if (lowerSender.contains("nagad")) {
            nagadCashIn.find(body)?.let { m ->
                val amount = m.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
                val trxId = m.groupValues[2]
                val type = when {
                    body.contains("merchant", ignoreCase = true) -> "Merchant"
                    body.contains("agent", ignoreCase = true) -> "Agent"
                    else -> "Personal"
                }
                return ParsedPayment("Nagad", type, amount, trxId)
            }
        }

        if (lowerSender.contains("bkash")) {
            bkashReceived.find(body)?.let { m ->
                val amount = m.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
                val trxId = m.groupValues[2]
                val type = when {
                    body.contains("merchant", ignoreCase = true) -> "Merchant"
                    body.contains("agent", ignoreCase = true) -> "Agent"
                    else -> "Personal"
                }
                return ParsedPayment("bKash", type, amount, trxId)
            }
        }

        return null
    }
}
