package com.mnmyounus.ymr.service
import android.app.*; import android.content.ContentValues; import android.content.Context; import android.content.Intent
import android.media.MediaRecorder; import android.net.Uri; import android.os.*; import android.provider.MediaStore
import androidx.core.app.NotificationCompat; import java.io.File; import java.text.SimpleDateFormat; import java.util.*

class CallRecorderService : Service() {
    companion object {
        const val ACTION_START = "ymr.START_RECORD"
        const val ACTION_STOP  = "ymr.STOP_RECORD"
        const val EXTRA_PITCH  = "pitch"   // 0=normal,1=high,2=low,3=robot
        var isRecording = false
        var currentPitch = 0
    }
    private var recorder: MediaRecorder? = null
    private var outFile: File? = null
    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        when(i?.action) {
            ACTION_START -> { currentPitch = i.getIntExtra(EXTRA_PITCH,0); startRecording() }
            ACTION_STOP  -> stopRecording()
        }
        return START_NOT_STICKY
    }
    private fun startRecording() {
        if(isRecording) return
        createChannel()
        startForeground(3001, buildNotif("🔴 Recording call..."))
        val dir = getExternalFilesDir("YMR_Recordings") ?: filesDir
        dir.mkdirs()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        outFile = File(dir, "YMR_Call_$ts.m4a")
        try {
            recorder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()
            recorder!!.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outFile!!.absolutePath)
                prepare(); start()
            }
            isRecording = true
        } catch(e: Exception) {
            // VOICE_COMMUNICATION fallback to MIC
            try {
                recorder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()
                recorder!!.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(outFile!!.absolutePath)
                    prepare(); start()
                }
                isRecording = true
            } catch(e2: Exception) { stopSelf() }
        }
    }
    private fun stopRecording() {
        if(!isRecording) return
        try { recorder?.stop(); recorder?.release() } catch(_: Exception) {}
        recorder = null; isRecording = false
        outFile?.let { saveToMediaStore(it) }
        stopForeground(true); stopSelf()
    }
    private fun saveToMediaStore(f: File) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cv = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, f.name)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/YMR Recordings")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), cv) ?: return
                contentResolver.openOutputStream(uri)?.use { out -> f.inputStream().use { it.copyTo(out) } }
                cv.clear(); cv.put(MediaStore.Audio.Media.IS_PENDING, 0); contentResolver.update(uri, cv, null, null)
            } else {
                val dest = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "YMR Recordings")
                dest.mkdirs(); f.copyTo(File(dest, f.name), overwrite = true)
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)))
            }
        } catch(_: Exception) {}
    }
    private fun createChannel() {
        val chan = NotificationChannel("ymr_rec","YMR Call Recorder",NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(chan)
    }
    private fun buildNotif(text: String) = NotificationCompat.Builder(this,"ymr_rec")
        .setContentTitle("YMR").setContentText(text)
        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
        .setPriority(NotificationCompat.PRIORITY_LOW).build()
    override fun onDestroy() { if(isRecording) stopRecording(); super.onDestroy() }
}
