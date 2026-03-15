package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.databinding.ItemMessageBinding
import com.mnmyounus.ymr.util.CryptoUtil; import com.mnmyounus.ymr.viewmodel.MainViewModel
import java.text.SimpleDateFormat; import java.util.*

class MessageAdapter : ListAdapter<MessageEntity, MessageAdapter.VH>(object: DiffUtil.ItemCallback<MessageEntity>() {
    override fun areItemsTheSame(a: MessageEntity, b: MessageEntity) = a.id==b.id
    override fun areContentsTheSame(a: MessageEntity, b: MessageEntity) = a==b
}) {
    inner class VH(val b: ItemMessageBinding): RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemMessageBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.b.tvMessage.text = MainViewModel.sessionPw?.let{CryptoUtil.decrypt(item.messageText,it)?:"🔒 Encrypted"}?:"🔒"
        h.b.tvTime.text = SimpleDateFormat("dd MMM · hh:mm a",Locale.getDefault()).format(Date(item.timestamp))
        h.b.tvSender.visibility = if(item.isGroup) View.VISIBLE else View.GONE
        h.b.tvSender.text = item.senderName
    }
}
