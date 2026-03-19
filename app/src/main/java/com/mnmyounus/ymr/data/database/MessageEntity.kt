package com.mnmyounus.ymr.data.database
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val senderName: String,
    val messageText: String,   // plain text - no encryption
    val timestamp: Long,
    val isGroup: Boolean = false
)
