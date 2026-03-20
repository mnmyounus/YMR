package com.mnmyounus.ymr.ui.features
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.DocumentAdapter
import com.mnmyounus.ymr.adapter.MediaFileAdapter
import com.mnmyounus.ymr.databinding.FragmentMediaListBinding
import com.mnmyounus.ymr.util.SaveUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MediaListFragment : Fragment() {
    private var _b: FragmentMediaListBinding? = null
    private val b get() = _b!!

    companion object {
        fun newInstance(type: String) = MediaListFragment().apply {
            arguments = Bundle().apply { putString("type", type) }
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMediaListBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        val type = arguments?.getString("type") ?: "photos"
        b.tvEmpty.text = "Loading..."
        b.tvEmpty.visibility = View.VISIBLE
        b.recycler.visibility = View.GONE

        when (type) {
            "photos", "videos" -> {
                b.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
                b.recycler.adapter = MediaFileAdapter { f -> SaveUtil.saveFile(requireContext(), f, "YMR Media") }
            }
            else -> {
                b.recycler.layoutManager = LinearLayoutManager(requireContext())
                b.recycler.adapter = DocumentAdapter { f -> SaveUtil.saveFile(requireContext(), f, "YMR Media") }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) { load(requireContext(), type) }
            if (!isAdded) return@launch
            when (type) {
                "photos", "videos" -> (b.recycler.adapter as? MediaFileAdapter)?.submitList(files)
                else               -> (b.recycler.adapter as? DocumentAdapter)?.submitList(files)
            }
            b.tvEmpty.visibility  = if (files.isEmpty()) View.VISIBLE else View.GONE
            b.recycler.visibility = if (files.isNotEmpty()) View.VISIBLE else View.GONE
            if (files.isEmpty()) b.tvEmpty.text = "No files found in WhatsApp/Telegram"
        }
    }

    private fun load(ctx: Context, type: String): List<File> {
        // Use direct file listing — faster and no deleted files
        val sd = Environment.getExternalStorageDirectory()
        val dirs = when (type) {
            "photos" -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Images"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images"),
                File(sd, "WhatsApp Business/Media/WhatsApp Images"),
                File(sd, "Telegram/Telegram Images")
            )
            "videos" -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Video"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"),
                File(sd, "WhatsApp Business/Media/WhatsApp Video"),
                File(sd, "Telegram/Telegram Video")
            )
            "audio" -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Audio"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio"),
                File(sd, "Telegram/Telegram Audio")
            )
            "docs" -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Documents"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"),
                File(sd, "Telegram/Telegram Documents"),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
            else -> emptyList()
        }

        val exts = when (type) {
            "photos" -> setOf("jpg", "jpeg", "png", "webp", "gif")
            "videos" -> setOf("mp4", "3gp", "mkv")
            "audio"  -> setOf("mp3", "aac", "ogg", "opus", "m4a", "wav")
            "docs"   -> setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip")
            else     -> emptySet()
        }

        return dirs
            .filter { it.exists() && it.canRead() }
            .flatMap { dir ->
                // listFiles only (no recursion) — fast and targeted
                dir.listFiles()
                    ?.filter { it.isFile && !it.name.startsWith(".") && it.extension.lowercase() in exts }
                    ?: emptyList()
            }
            .distinctBy { "${it.name}_${it.length()}" }
            .sortedByDescending { it.lastModified() }
            .take(80)  // max 80 per tab — enough, no memory pressure
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
