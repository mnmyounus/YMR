package com.mnmyounus.ymr.adapter
import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.databinding.ItemMessageBinding
import java.text.SimpleDateFormat; import java.util.*

class MessageAdapter : ListAdapter<MessageEntity, MessageAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(a: MessageEntity, b: MessageEntity) = a.id == b.id
            override fun areContentsTheSame(a: MessageEntity, b: MessageEntity) = a == b
        }
    }

    inner class VH(val b: ItemMessageBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.b.tvMessage.text = item.messageText   // plain text, instant
        h.b.tvTime.text    = SimpleDateFormat("dd MMM · hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
        h.b.tvSender.visibility = if (item.isGroup) View.VISIBLE else View.GONE
        h.b.tvSender.text  = item.senderName
    }
}
