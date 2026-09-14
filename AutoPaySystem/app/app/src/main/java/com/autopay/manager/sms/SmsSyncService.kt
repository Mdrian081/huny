package com.autopay.manager.sms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.autopay.manager.data.FirebaseRepository
import com.autopay.manager.data.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSyncService : Service() {

    companion object {
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
        private const val CHANNEL_ID = "sms_sync_channel"
        private const val NOTIF_ID = 42
    }

    private val repository = FirebaseRepository()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())

        val sender = intent?.getStringExtra(EXTRA_SENDER)
        val body = intent?.getStringExtra(EXTRA_BODY)

        if (sender != null && body != null) {
            scope.launch {
                handleIncomingSms(sender, body)
                stopSelf(startId)
            }
        } else {
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    private suspend fun handleIncomingSms(sender: String, body: String) {
        val parsed = SmsParser.parse(sender, body)

        val tx = if (parsed != null) {
            // Avoid storing the exact same transaction id twice.
            if (repository.transactionExists(parsed.method, parsed.trxId)) return

            Transaction(
                method = parsed.method,
                type = parsed.type,
                trxId = parsed.trxId,
                amount = parsed.amount,
                senderNumber = sender,
                rawMessage = body,
                status = "unused"
            )
        } else {
            // Unknown format - still stored so nothing is silently lost,
            // flagged for manual review from the Transactions screen.
            Transaction(
                method = "Unknown",
                type = "--",
                trxId = "--",
                amount = 0.0,
                senderNumber = sender,
                rawMessage = body,
                status = "awaiting-review"
            )
        }

        repository.addTransaction(tx)
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Payment sync",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoPay Manager")
            .setContentText("Syncing a new payment message…")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
