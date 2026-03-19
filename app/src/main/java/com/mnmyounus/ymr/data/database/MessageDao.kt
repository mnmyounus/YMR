package com.mnmyounus.ymr.data.database
import androidx.lifecycle.LiveData; import androidx.room.*

data class ConversationSummary(
    val packageName: String, val appName: String, val senderName: String,
    val lastMessage: String, val lastTimestamp: Long, val messageCount: Int
)

@Dao interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: MessageEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(m: List<MessageEntity>)
    @Query("SELECT * FROM messages WHERE packageName=:pkg AND senderName=:sender ORDER BY timestamp ASC")
    fun getChat(pkg: String, sender: String): LiveData<List<MessageEntity>>
    @Query("""SELECT packageName,appName,senderName,MAX(messageText) AS lastMessage,
        MAX(timestamp) AS lastTimestamp,COUNT(*) AS messageCount
        FROM messages GROUP BY packageName,senderName ORDER BY lastTimestamp DESC""")
    fun getConversations(): LiveData<List<ConversationSummary>>
    @Query("SELECT COUNT(*) FROM messages") fun getTotalCount(): LiveData<Int>
    @Query("SELECT * FROM messages ORDER BY timestamp ASC") suspend fun getAllMessages(): List<MessageEntity>
    @Query("DELETE FROM messages") suspend fun deleteAll()
}
