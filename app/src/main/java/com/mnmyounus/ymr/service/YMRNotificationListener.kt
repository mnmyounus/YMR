package com.mnmyounus.ymr.service
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mnmyounus.ymr.data.database.AppDatabase
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.util.PrefsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YMRNotificationListener : NotificationListenerService() {
    companion object {
        val APPS = mapOf(
            "com.whatsapp"               to "WhatsApp",
            "com.whatsapp.w4b"           to "WhatsApp Business",
            "org.telegram.messenger"     to "Telegram",
            "org.telegram.messenger.web" to "Telegram X",
            "com.facebook.orca"          to "Messenger",
            "com.instagram.android"      to "Instagram",
            "com.viber.voip"             to "Viber",
            "com.snapchat.android"       to "Snapchat",
            "kik.android"                to "Kik",
            "com.discord"                to "Discord",
            "com.skype.raider"           to "Skype",
            "com.microsoft.teams"        to "Teams",
            "jp.naver.line.android"      to "LINE"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (!PrefsUtil.isSvcEnabled(applicationContext)) return
        val appName = APPS[sbn.packageName] ?: return

        // Filter by selected app if set
        val filter = PrefsUtil.getFilter(applicationContext)
        if (filter.isNotEmpty() && sbn.packageName != filter) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title")
            ?: extras.getCharSequence("android.title")?.toString() ?: return
        val raw  = extras.getString("android.text")
            ?: extras.getCharSequence("android.text")?.toString()
        val big  = extras.getCharSequence("android.bigText")?.toString()
        val text = if (!big.isNullOrBlank() && big.length > (raw?.length ?: 0)) big else (raw ?: return)
        if (text.isBlank()) return

        // Label special media types clearly
        val labelledText = when (text.trim().lowercase()) {
            "image", "photo"              -> "📷 Image"
            "video"                       -> "🎥 Video"
            "sticker"                     -> "🎨 Sticker"
            "audio", "voice message","ptt"-> "🎵 Voice/Audio"
            "document", "file"            -> "📄 Document"
            "gif"                         -> "🎞 GIF"
            "contact"                     -> "👤 Contact"
            "location"                    -> "📍 Location"
            else -> if (text.startsWith("view once", ignoreCase = true)) "👁 View Once" else text
        }

        scope.launch {
            AppDatabase.get(applicationContext).messageDao().insert(
                MessageEntity(
                    packageName = sbn.packageName,
                    appName     = appName,
                    senderName  = title,
                    messageText = labelledText,   // plain text, no encryption
                    timestamp   = System.currentTimeMillis(),
                    isGroup     = title.contains(":") && !title.startsWith("+")
                )
            )
        }
    }
}
