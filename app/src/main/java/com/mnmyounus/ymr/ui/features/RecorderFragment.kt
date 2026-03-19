package com.mnmyounus.ymr.ui.features
import android.Manifest; import android.content.Intent; import android.content.pm.PackageManager
import android.media.MediaPlayer; import android.os.Build; import android.os.Bundle
import android.os.Environment; import android.view.*; import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat; import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.RecordingAdapter
import com.mnmyounus.ymr.databinding.FragmentRecorderBinding
import com.mnmyounus.ymr.service.CallRecorderService
import java.io.File

class RecorderFragment : Fragment() {
    private var _b: FragmentRecorderBinding? = null; private val b get() = _b!!
    private var player: MediaPlayer? = null
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if(granted) toggleRecording() else Toast.makeText(requireContext(),"Microphone permission required",Toast.LENGTH_SHORT).show()
    }
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentRecorderBinding.inflate(i,c,false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val adapter = RecordingAdapter { file -> playRecording(file) }
        b.recyclerRecordings.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerRecordings.adapter = adapter
        loadRecordings(adapter)

        b.btnToggleRecord.setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
                permLauncher.launch(Manifest.permission.RECORD_AUDIO)
            else toggleRecording()
        }

        // Voice changer buttons
        b.btnNormal.setOnClickListener { CallRecorderService.currentPitch=0; highlightBtn(0) }
        b.btnHigh.setOnClickListener   { CallRecorderService.currentPitch=1; highlightBtn(1) }
        b.btnLow.setOnClickListener    { CallRecorderService.currentPitch=2; highlightBtn(2) }
        b.btnRobot.setOnClickListener  { CallRecorderService.currentPitch=3; highlightBtn(3) }
        highlightBtn(CallRecorderService.currentPitch)
        updateUI()
    }

    private fun toggleRecording() {
        val ctx = requireContext()
        if(CallRecorderService.isRecording) {
            ctx.stopService(Intent(ctx, CallRecorderService::class.java))
            CallRecorderService.isRecording = false
        } else {
            val i = Intent(ctx, CallRecorderService::class.java).apply {
                action = CallRecorderService.ACTION_START
                putExtra(CallRecorderService.EXTRA_PITCH, CallRecorderService.currentPitch)
            }
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) ctx.startForegroundService(i) else ctx.startService(i)
        }
        // Small delay then update UI
        b.root.postDelayed({ updateUI(); loadRecordings(b.recyclerRecordings.adapter as RecordingAdapter) }, 500)
    }

    private fun updateUI() {
        if(CallRecorderService.isRecording) {
            b.tvRecordingStatus.text = "● Recording..."
            b.tvRecordingStatus.setTextColor(requireContext().getColor(com.mnmyounus.ymr.R.color.red_warn))
            b.btnToggleRecord.text = getString(com.mnmyounus.ymr.R.string.stop_record)
        } else {
            b.tvRecordingStatus.text = "Not Recording"
            b.tvRecordingStatus.setTextColor(requireContext().getColor(com.mnmyounus.ymr.R.color.green_ok))
            b.btnToggleRecord.text = getString(com.mnmyounus.ymr.R.string.start_record)
        }
    }

    private fun loadRecordings(adapter: RecordingAdapter) {
        val dir = requireContext().getExternalFilesDir("YMR_Recordings") ?: return
        val files = dir.listFiles()?.filter{it.extension.lowercase()=="m4a"}
            ?.sortedByDescending{it.lastModified()} ?: emptyList()
        adapter.submitList(files)
        b.tvNoRecordings.visibility = if(files.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun playRecording(file: File) {
        player?.stop(); player?.release()
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath); prepare(); start()
            setOnCompletionListener { release() }
        }
        Toast.makeText(requireContext(),"Playing: ${file.name}", Toast.LENGTH_SHORT).show()
    }

    private fun highlightBtn(pitch: Int) {
        val btns = listOf(b.btnNormal, b.btnHigh, b.btnLow, b.btnRobot)
        btns.forEachIndexed { i, btn ->
            btn.isSelected = i == pitch
            btn.alpha = if(i==pitch) 1f else 0.6f
        }
    }

    override fun onDestroyView() {
        player?.stop(); player?.release(); player = null
        super.onDestroyView(); _b = null
    }
}
