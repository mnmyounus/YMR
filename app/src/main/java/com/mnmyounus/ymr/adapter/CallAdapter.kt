package com.mnmyounus.ymr.adapter
import android.provider.CallLog; import android.view.*; import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.ItemCallBinding
import java.text.SimpleDateFormat; import java.util.*; import java.util.concurrent.TimeUnit

data class CallRecord(val name: String, val number: String, val type: Int, val date: Long, val duration: Long)

class CallAdapter : ListAdapter<CallRecord, CallAdapter.VH>(object: DiffUtil.ItemCallback<CallRecord>() {
    override fun areItemsTheSame(a: CallRecord, b: CallRecord) = a.date==b.date&&a.number==b.number
    override fun areContentsTheSame(a: CallRecord, b: CallRecord) = a==b
}) {
    companion object { const val INCOMING=1; const val OUTGOING=2; const val MISSED=3 }
    inner class VH(val b: ItemCallBinding): RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemCallBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val c = getItem(pos)
        h.b.tvName.text = c.name.ifEmpty{c.number}; h.b.tvNumber.text = c.number
        h.b.tvDate.text = SimpleDateFormat("dd MMM hh:mm a",Locale.getDefault()).format(Date(c.date))
        val dur = c.duration; h.b.tvDuration.text = if(dur>0)"${dur/60}m ${dur%60}s" else ""
        val icon = when(c.type){ CallLog.Calls.INCOMING_TYPE->R.drawable.ic_phone_in; CallLog.Calls.OUTGOING_TYPE->R.drawable.ic_phone_out; else->R.drawable.ic_phone_missed }
        h.b.ivCallType.setImageResource(icon)
    }
}
