package com.mnmyounus.ymr.service
import android.app.*; import android.content.ContentValues; import android.content.Intent
import android.os.*; import android.provider.MediaStore
import androidx.core.app.NotificationCompat; import java.io.File

class MediaWatcherService : Service() {
    private val obs = mutableListOf<RecursiveFileObserver>()
    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        createNotif(); startForeground(2001, buildNotif()); watch(); return START_STICKY
    }
    private fun watch() {
        obs.forEach{it.stopWatching()}; obs.clear()
        val sd = Environment.getExternalStorageDirectory()
        listOf("WhatsApp/Media/WhatsApp Images","WhatsApp/Media/WhatsApp Video",
            "WhatsApp/Media/WhatsApp Documents","WhatsApp/Media/WhatsApp Audio",
            "WhatsApp/Media/WhatsApp Images/Private",
            "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images",
            "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video",
            "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents",
            "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/Private",
            "WhatsApp Business/Media/WhatsApp Images","WhatsApp Business/Media/WhatsApp Documents"
        ).forEach { path ->
            val dir = File(sd, path); dir.mkdirs()
            runCatching { RecursiveFileObserver(dir.absolutePath){f->save(f)}.also{it.startWatching();obs.add(it)} }
        }
    }
    private fun save(src: File) {
        runCatching {
            if(!src.exists()||!src.canRead()||src.name.startsWith(".")) return
            val ext = src.extension.lowercase()
            if(ext !in setOf("jpg","jpeg","png","mp4","3gp","pdf","doc","docx","mp3","aac","ogg","opus")) return
            val vO = src.absolutePath.contains("Private",ignoreCase=true)
            val folder = if(vO)"YMR ViewOnce" else "YMR AutoSaved"
            val isV = ext in listOf("mp4","3gp"); val isI = ext in listOf("jpg","jpeg","png")
            val mime = when{isV->"video/$ext";isI->if(ext=="jpg")"image/jpeg" else "image/$ext";else->"application/octet-stream"}
            val name="${folder.replace(" ","_")}_${System.currentTimeMillis()}.$ext"
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                val col=if(isV)MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        else MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val cv=ContentValues().apply{
                    put(MediaStore.MediaColumns.DISPLAY_NAME,name);put(MediaStore.MediaColumns.MIME_TYPE,mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH,if(isV)"Movies/$folder" else "Pictures/$folder")
                    put(MediaStore.MediaColumns.IS_PENDING,1)}
                val uri=contentResolver.insert(col,cv)?:return
                contentResolver.openOutputStream(uri)?.use{o->src.inputStream().use{it.copyTo(o)}}
                cv.clear();cv.put(MediaStore.MediaColumns.IS_PENDING,0);contentResolver.update(uri,cv,null,null)
            } else {
                val dest=File(Environment.getExternalStoragePublicDirectory(
                    if(isV)Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES),folder)
                dest.mkdirs();src.copyTo(File(dest,name),overwrite=false)
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,android.net.Uri.fromFile(File(dest,name))))
            }
        }
    }
    private fun createNotif() {
        val chan=NotificationChannel("ymr_watch","YMR Watcher",NotificationManager.IMPORTANCE_MIN)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(chan)
    }
    private fun buildNotif() = NotificationCompat.Builder(this,"ymr_watch")
        .setContentTitle("YMR").setContentText("Watching for new media")
        .setSmallIcon(android.R.drawable.ic_menu_gallery).setPriority(NotificationCompat.PRIORITY_MIN).build()
    override fun onDestroy() { obs.forEach{it.stopWatching()}; super.onDestroy() }
}
