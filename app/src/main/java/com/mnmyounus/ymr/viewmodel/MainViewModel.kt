package com.mnmyounus.ymr.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel; import androidx.lifecycle.viewModelScope
import com.mnmyounus.ymr.data.repository.MessageRepository
import kotlinx.coroutines.launch
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MessageRepository(app)
    val conversations = repo.conversations
    val totalCount = repo.totalCount
    fun getChat(pkg: String, sender: String) = repo.getChat(pkg, sender)
    fun decrypt(c: String) = sessionPw?.let { repo.decrypt(c, it) } ?: "🔒"
    fun deleteAll() = viewModelScope.launch { repo.deleteAll() }
    suspend fun getAllMessages() = repo.getAllMessages()
    suspend fun insertAll(msgs: List<com.mnmyounus.ymr.data.database.MessageEntity>) = repo.insertAll(msgs)
    companion object { var sessionPw: String? = null }
}
