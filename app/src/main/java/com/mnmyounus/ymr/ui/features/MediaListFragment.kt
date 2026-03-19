package com.mnmyounus.ymr.ui.features
import android.content.Context; import android.os.Bundle; import android.os.Environment
import android.provider.MediaStore; import android.view.*
import androidx.fragment.app.Fragment; import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager; import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.DocumentAdapter; import com.mnmyounus.ymr.adapter.MediaFileAdapter
import com.mnmyounus.ymr.databinding.FragmentMediaListBinding
import com.mnmyounus.ymr.util.SaveUtil
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext
import java.io.File

class MediaListFragment : Fragment() {
    private var _b: FragmentMediaListBinding? = null; private val b get() = _b!!
    companion object {
        fun newInstance(type: String) = MediaListFragment().apply {
            arguments = Bundle().apply { putString("type", type) }
        }
    }
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMediaListBinding.inflate(i,c,false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val type = arguments?.getString("type") ?: "photos"
        b.tvEmpty.text = "Loading..."; b.tvEmpty.visibility = View.VISIBLE; b.recycler.visibility = View.GONE
        when(type) {
            "photos","videos" -> {
                b.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
                b.recycler.adapter = MediaFileAdapter { f -> SaveUtil.saveFile(requireContext(), f, "YMR Media") }
            }
            else -> {
                b.recycler.layoutManager = LinearLayoutManager(requireContext())
                b.recycler.adapter = DocumentAdapter { f -> SaveUtil.saveFile(requireContext(), f, "YMR Media") }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) { query(requireContext(), type) }
            if (!isAdded) return@launch
            when(type) {
                "photos","videos" -> (b.recycler.adapter as? MediaFileAdapter)?.submitList(files)
                else              -> (b.recycler.adapter as? DocumentAdapter)?.submitList(files)
            }
            b.tvEmpty.visibility  = if(files.isEmpty()) View.VISIBLE else View.GONE
            b.recycler.visibility = if(files.isNotEmpty()) View.VISIBLE else View.GONE
            if(files.isEmpty()) b.tvEmpty.text = "No files found"
        }
    }
    private fun query(ctx: Context, type: String): List<File> {
        val result = mutableListOf<File>()
        try {
            when(type) {
                "photos" -> {
                    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val proj = arrayOf(MediaStore.Images.Media.DATA)
                    val sel = "${MediaStore.Images.Media.DATA} LIKE ? OR ${MediaStore.Images.Media.DATA} LIKE ?"
                    val args = arrayOf("%WhatsApp%","%Telegram%")
                    ctx.contentResolver.query(uri,proj,sel,args,"${MediaStore.Images.Media.DATE_MODIFIED} DESC")?.use{c->
                        val col=c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        while(c.moveToNext()){val f=File(c.getString(col)?:continue);if(f.exists())result.add(f)}
                    }
                }
                "videos" -> {
                    val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    val proj = arrayOf(MediaStore.Video.Media.DATA)
                    val sel = "${MediaStore.Video.Media.DATA} LIKE ? OR ${MediaStore.Video.Media.DATA} LIKE ?"
                    val args = arrayOf("%WhatsApp%","%Telegram%")
                    ctx.contentResolver.query(uri,proj,sel,args,"${MediaStore.Video.Media.DATE_MODIFIED} DESC")?.use{c->
                        val col=c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                        while(c.moveToNext()){val f=File(c.getString(col)?:continue);if(f.exists())result.add(f)}
                    }
                }
                "audio" -> {
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val proj = arrayOf(MediaStore.Audio.Media.DATA)
                    val sel = "${MediaStore.Audio.Media.DATA} LIKE ? OR ${MediaStore.Audio.Media.DATA} LIKE ?"
                    val args = arrayOf("%WhatsApp%","%Telegram%")
                    ctx.contentResolver.query(uri,proj,sel,args,"${MediaStore.Audio.Media.DATE_MODIFIED} DESC")?.use{c->
                        val col=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                        while(c.moveToNext()){val f=File(c.getString(col)?:continue);if(f.exists())result.add(f)}
                    }
                }
                "docs" -> {
                    val docExts = setOf("pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip")
                    val uri = MediaStore.Files.getContentUri("external")
                    val proj = arrayOf(MediaStore.Files.FileColumns.DATA)
                    val sel = "${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
                    val args = arrayOf("%WhatsApp Documents%","%Download%")
                    ctx.contentResolver.query(uri,proj,sel,args,"${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC")?.use{c->
                        val col=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                        while(c.moveToNext()){
                            val path=c.getString(col)?:continue
                            val f=File(path)
                            if(f.exists()&&f.isFile&&f.extension.lowercase() in docExts)result.add(f)
                        }
                    }
                }
            }
        } catch(_:Exception){}
        return result.distinctBy{"${it.name}_${it.length()}"}.sortedByDescending{it.lastModified()}.take(150)
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
