package com.mnmyounus.ymr.util
import android.content.ContentValues; import android.content.Context; import android.content.Intent
import android.net.Uri; import android.os.Build; import android.os.Environment; import android.provider.MediaStore
import android.widget.Toast; import java.io.File

object SaveUtil {
    fun saveFile(ctx: Context, file: File, folder: String = "YMR") {
        try {
            if (!file.exists() || !file.canRead()) { toast(ctx,"❌ Cannot read file"); return }
            val ext = file.extension.lowercase()
            val isVid = ext in setOf("mp4","3gp","mkv"); val isImg = ext in setOf("jpg","jpeg","png","gif","webp")
            val isAud = ext in setOf("mp3","aac","ogg","opus","m4a","wav")
            val mime = when { isVid->"video/$ext"; isImg->if(ext=="jpg")"image/jpeg" else "image/$ext"; isAud->"audio/$ext"; else->"application/octet-stream" }
            val name = "YMR_${System.currentTimeMillis()}.${file.extension}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val col = when { isVid->MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    isImg->MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    isAud->MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else->MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) }
                val rel = when { isVid->"Movies/$folder"; isImg->"Pictures/$folder"; isAud->"Music/$folder"; else->"Download/$folder" }
                val cv = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name); put(MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, rel); put(MediaStore.MediaColumns.IS_PENDING, 1) }
                val uri = ctx.contentResolver.insert(col, cv) ?: throw Exception("Insert failed")
                ctx.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
                cv.clear(); cv.put(MediaStore.MediaColumns.IS_PENDING, 0); ctx.contentResolver.update(uri, cv, null, null)
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(
                    when { isVid->Environment.DIRECTORY_MOVIES; isAud->Environment.DIRECTORY_MUSIC; else->Environment.DIRECTORY_PICTURES }), folder)
                dir.mkdirs(); val dest = File(dir, name); file.copyTo(dest, overwrite=true)
                ctx.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest)))
            }
            toast(ctx,"✅ Saved!")
        } catch(e: Exception) { toast(ctx,"❌ ${e.message}") }
    }
    private fun toast(ctx: Context, msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}
