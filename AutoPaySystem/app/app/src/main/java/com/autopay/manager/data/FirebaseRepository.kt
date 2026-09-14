package com.autopay.manager.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Single point of contact with Firebase Firestore.
 * Collections used:
 *   transactions/{id}
 *   withdrawals/{id}
 *   topups/{id}
 *   balances/{method}
 */
class FirebaseRepository(private val db: FirebaseFirestore) {

    private val transactionsRef = db.collection("transactions")
    private val withdrawalsRef = db.collection("withdrawals")
    private val topupsRef = db.collection("topups")
    private val balancesRef = db.collection("balances")

    // ---------- Transactions ----------

    suspend fun addTransaction(tx: Transaction) {
        transactionsRef.add(tx).await()
    }

    /** Prevents the same trx_id + method from being stored twice. */
    suspend fun transactionExists(method: String, trxId: String): Boolean {
        val snap = transactionsRef
            .whereEqualTo("method", method)
            .whereEqualTo("trxId", trxId)
            .limit(1)
            .get()
            .await()
        return !snap.isEmpty
    }

    fun observeTransactions(): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                val items = snap?.toObjects(Transaction::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markTransactionUsed(id: String) {
        transactionsRef.document(id).update("status", "used").await()
    }

    // ---------- Withdrawals ----------

    suspend fun requestWithdraw(request: WithdrawRequest) {
        withdrawalsRef.add(request).await()
    }

    fun observeWithdrawals(): Flow<List<WithdrawRequest>> = callbackFlow {
        val listener = withdrawalsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(WithdrawRequest::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // ---------- Top-up orders ----------

    suspend fun addTopUpOrder(order: TopUpOrder) {
        topupsRef.add(order).await()
    }

    fun observeTopUpOrders(): Flow<List<TopUpOrder>> = callbackFlow {
        val listener = topupsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(TopUpOrder::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // ---------- Balances ----------

    fun observeBalances(): Flow<List<BalanceInfo>> = callbackFlow {
        val listener = balancesRef.addSnapshotListener { snap, _ ->
            trySend(snap?.toObjects(BalanceInfo::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun setBalance(info: BalanceInfo) {
        balancesRef.document(info.method).set(info).await()
    }

    // ---------- API access key (used by the website's verify-payment calls) ----------

    private val apiConfigRef = db.collection("config").document("apiAccess")

    suspend fun getApiKey(): String? {
        val doc = apiConfigRef.get().await()
        return doc.getString("key")
    }

    suspend fun setApiKey(key: String) {
        apiConfigRef.set(mapOf("key" to key)).await()
    }
}
