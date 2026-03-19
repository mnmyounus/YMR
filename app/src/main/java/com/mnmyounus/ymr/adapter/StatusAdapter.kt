package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.mnmyounus.ymr.databinding.ItemStatusBinding; import java.io.File

class StatusAdapter(private val onClick: (File)->Unit, private val onSave: (File)->Unit)
    : ListAdapter<File, StatusAdapter.VH>(object: DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(a: File, b: File) = a.path==b.path
        override fun areContentsTheSame(a: File, b: File) = a.lastModified()==b.lastModified()
    }) {
    inner class VH(val b: ItemStatusBinding): RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemStatusBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val f = getItem(pos); val isVid = f.name.endsWith(".mp4",true)
        h.b.ivPlay.visibility = if(isVid) View.VISIBLE else View.GONE
        Glide.with(h.b.root.context).load(f).centerCrop().into(h.b.ivThumbnail)
        h.b.root.setOnClickListener{onClick(f)}; h.b.btnSave.setOnClickListener{onSave(f)}
    }
}
