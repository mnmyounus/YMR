package com.mnmyounus.ymr.adapter
import android.view.LayoutInflater; import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.data.database.ConversationSummary
import com.mnmyounus.ymr.databinding.ItemConversationBinding
import java.text.SimpleDateFormat; import java.util.*

class ConversationAdapter(private val onClick: (ConversationSummary) -> Unit)
    : ListAdapter<ConversationSummary, ConversationAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ConversationSummary>() {
            override fun areItemsTheSame(a: ConversationSummary, b: ConversationSummary) =
                a.packageName == b.packageName && a.senderName == b.senderName
            override fun areContentsTheSame(a: ConversationSummary, b: ConversationSummary) = a == b
        }
    }

    inner class VH(val b: ItemConversationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.b.tvName.text     = item.senderName
        h.b.tvInitials.text = item.senderName.take(1).uppercase()
        h.b.tvAppName.text  = item.appName
        h.b.tvTime.text     = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(item.lastTimestamp))
        h.b.tvLastMsg.text  = item.lastMessage   // plain text, no decrypt
        h.b.tvBadge.visibility = if (item.messageCount > 0) android.view.View.VISIBLE else android.view.View.GONE
        h.b.tvBadge.text = item.messageCount.toString()
        h.b.root.setOnClickListener { onClick(item) }
    }
}
