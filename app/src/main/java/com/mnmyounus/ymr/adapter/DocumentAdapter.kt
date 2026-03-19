package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.ItemDocumentBinding; import java.io.File

class DocumentAdapter(private val onSave: (File)->Unit)
    : ListAdapter<File, DocumentAdapter.VH>(object: DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(a: File, b: File) = a.path==b.path
        override fun areContentsTheSame(a: File, b: File) = a.lastModified()==b.lastModified()
    }) {
    inner class VH(val b: ItemDocumentBinding): RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemDocumentBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val f = getItem(pos); val isAud = f.extension.lowercase() in listOf("mp3","aac","ogg","opus","m4a","wav")
        h.b.ivIcon.setImageResource(if(isAud) R.drawable.ic_audio else R.drawable.ic_document)
        h.b.tvName.text = f.name
        h.b.tvSize.text = when{ f.length()>=1_048_576->"%.1f MB".format(f.length()/1_048_576.0); f.length()>=1024->"%.1f KB".format(f.length()/1024.0); else->"${f.length()} B" }
        h.b.btnSave.setOnClickListener{onSave(f)}
    }
}
