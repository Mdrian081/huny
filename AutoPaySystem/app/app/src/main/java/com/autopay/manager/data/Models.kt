package com.autopay.manager.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** One parsed (or manually entered) payment transaction. */
data class Transaction(
    @DocumentId val id: String = "",
    val method: String = "",          // "Nagad", "bKash", "Manual", ...
    val type: String = "",            // "Personal", "Merchant", "Agent"
    val trxId: String = "",
    val amount: Double = 0.0,
    val senderNumber: String = "",
    val rawMessage: String = "",
    val status: String = "unused",    // unused | used | awaiting-review | error
    val note: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

/** A withdrawal request raised from the app. */
data class WithdrawRequest(
    @DocumentId val id: String = "",
    val amount: Double = 0.0,
    val destinationMethod: String = "",   // "Nagad", "bKash", "Bank"
    val destinationNumber: String = "",
    val status: String = "pending",       // pending | approved | rejected
    val note: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

/** A top-up / order item, e.g. a Free Fire diamond package purchase. */
data class TopUpOrder(
    @DocumentId val id: String = "",
    val product: String = "",         // e.g. "Free Fire 100 Diamonds"
    val playerId: String = "",
    val price: Double = 0.0,
    val trxId: String = "",
    val status: String = "pending",   // pending | delivered | rejected
    @ServerTimestamp val createdAt: Date? = null
)

/** Running wallet balance, one document per payment method. */
data class BalanceInfo(
    val method: String = "",
    val currentBalance: Double = 0.0,
    val simSlot: String = "Any"
)
