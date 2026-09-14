package com.autopay.manager.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.content.ContextCompat

/**
 * Fires whenever THIS device receives a new SMS (e.g. a Nagad/bKash
 * "payment received" confirmation). It never sends or fabricates SMS -
 * it only reads what already arrived on this phone, then hands it off
 * to SmsSyncService to parse + upload to Firebase.
 */
class IncomingSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val sender = sms.originatingAddress ?: continue
            val body = sms.messageBody ?: continue

            val serviceIntent = Intent(context, SmsSyncService::class.java).apply {
                putExtra(SmsSyncService.EXTRA_SENDER, sender)
                putExtra(SmsSyncService.EXTRA_BODY, body)
                putExtra(SmsSyncService.EXTRA_TIMESTAMP, sms.timestampMillis)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
