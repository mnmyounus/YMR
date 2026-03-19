package com.mnmyounus.ymr.service
import android.os.Build
import android.os.FileObserver
import java.io.File

class RecursiveFileObserver(private val root: String, private val onFile: (File) -> Unit) {
    private val obs = mutableListOf<FileObserver>()

    fun startWatching() { stopWatching(); watch(File(root)) }

    private fun watch(dir: File) {
        if (!dir.exists()) return
        val mask = FileObserver.CREATE or FileObserver.MOVED_TO
        val o = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(dir, mask) {
                override fun onEvent(e: Int, p: String?) { p?.let { handle(File(dir, it), e) } }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(dir.absolutePath, mask) {
                override fun onEvent(e: Int, p: String?) { p?.let { handle(File(dir, it), e) } }
            }
        }
        o.startWatching(); obs.add(o)
        dir.listFiles()?.filter { it.isDirectory }?.forEach { watch(it) }
    }

    private fun handle(f: File, e: Int) {
        val mask = FileObserver.CREATE or FileObserver.MOVED_TO
        if (f.isDirectory) watch(f)
        else if (e and mask != 0) onFile(f)
    }

    fun stopWatching() { obs.forEach { it.stopWatching() }; obs.clear() }
}
