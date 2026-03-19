package com.mnmyounus.ymr.data.repository
import android.content.Context
import com.mnmyounus.ymr.data.database.AppDatabase
import com.mnmyounus.ymr.data.database.MessageEntity

class MessageRepository(ctx: Context) {
    private val dao = AppDatabase.get(ctx).messageDao()
    val conversations = dao.getConversations()
    val totalCount    = dao.getTotalCount()
    fun getChat(pkg: String, sender: String) = dao.getChat(pkg, sender)
    suspend fun deleteAll()                      = dao.deleteAll()
    suspend fun getAllMessages()                  = dao.getAllMessages()
    suspend fun insertAll(msgs: List<MessageEntity>) = dao.insertAll(msgs)
}
