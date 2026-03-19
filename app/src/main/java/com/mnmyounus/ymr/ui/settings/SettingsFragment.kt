package com.mnmyounus.ymr.ui.settings
import android.Manifest; import android.app.AlertDialog; import android.content.ComponentName
import android.content.Intent; import android.content.pm.PackageManager; import android.graphics.Color
import android.net.Uri; import android.os.Build; import android.os.Bundle; import android.provider.Settings
import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat; import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels; import androidx.lifecycle.lifecycleScope
import com.mnmyounus.ymr.R; import com.mnmyounus.ymr.YMRApp
import com.mnmyounus.ymr.data.database.AppDatabase
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.databinding.FragmentSettingsBinding
import com.mnmyounus.ymr.util.PrefsUtil
import com.mnmyounus.ymr.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext
import org.json.JSONArray; import org.json.JSONObject; import java.io.File

class SettingsFragment : Fragment() {
    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refreshPermDots() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSettingsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        val ctx = requireContext()

        // Theme toggle
        b.switchTheme.isChecked = PrefsUtil.isDarkTheme(ctx)
        b.tvThemeStatus.text = if (PrefsUtil.isDarkTheme(ctx)) "Dark Mode" else "Light Mode"
        b.switchTheme.setOnCheckedChangeListener { _, on ->
            PrefsUtil.setDarkTheme(ctx, on)
            b.tvThemeStatus.text = if (on) "Dark Mode" else "Light Mode"
            (requireActivity().application as YMRApp).applyTheme()
        }

        // Service switch
        b.switchSvc.isChecked = PrefsUtil.isSvcEnabled(ctx)
        b.switchSvc.setOnCheckedChangeListener { _, on ->
            PrefsUtil.setSvcEnabled(ctx, on)
            b.tvSvcStatus.text = if (on) getString(R.string.svc_on) else getString(R.string.svc_off)
        }

        // Permission dots
        refreshPermDots()
        b.btnGrantNotif.setOnClickListener   { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        b.btnGrantStorage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 30)
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${ctx.packageName}")))
            else permLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
        b.btnGrantAudio.setOnClickListener { permLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO)) }
        b.btnGrantCall.setOnClickListener  { permLauncher.launch(arrayOf(Manifest.permission.READ_CALL_LOG)) }

        // Change icon
        b.btnChangeIcon.setOnClickListener {
            AlertDialog.Builder(ctx).setTitle(getString(R.string.change_icon))
                .setItems(arrayOf("Icon 1 (ymr1.jpg)", "Icon 2 (ymr2.jpg)", "Icon 3 (ymr3.jpg)", "Icon 4 (ymr4.jpg)", "Icon 5 (ymr5.jpg)")) { _, _ ->
                    Toast.makeText(ctx, "Place ymr1-5.jpg in res/drawable and rebuild", Toast.LENGTH_LONG).show()
                }.show()
        }

        // Hide password button since AES removed
        b.btnChangePw.visibility = View.GONE

        // Export (plain text JSON)
        b.btnExport.setOnClickListener {
            lifecycleScope.launch {
                val msgs = withContext(Dispatchers.IO) { AppDatabase.get(ctx).messageDao().getAllMessages() }
                val json = JSONArray()
                msgs.forEach { msg ->
                    json.put(JSONObject().apply {
                        put("pkg", msg.packageName); put("app", msg.appName)
                        put("sender", msg.senderName); put("msg", msg.messageText)
                        put("ts", msg.timestamp);     put("group", msg.isGroup)
                    })
                }
                val file = File(ctx.getExternalFilesDir(null), "YMR_backup.json")
                withContext(Dispatchers.IO) { file.writeText(json.toString()) }
                Toast.makeText(ctx, "Exported to: ${file.path}", Toast.LENGTH_LONG).show()
            }
        }

        // Import (plain text JSON)
        b.btnImport.setOnClickListener {
            AlertDialog.Builder(ctx).setTitle("Import").setMessage("Import from YMR_backup.json?")
                .setPositiveButton("Import") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            val file = File(ctx.getExternalFilesDir(null), "YMR_backup.json")
                            if (!file.exists()) { Toast.makeText(ctx, "No backup found", Toast.LENGTH_SHORT).show(); return@launch }
                            val json = JSONArray(file.readText())
                            val msgs = mutableListOf<MessageEntity>()
                            for (i in 0 until json.length()) {
                                val o = json.getJSONObject(i)
                                msgs.add(MessageEntity(
                                    packageName = o.getString("pkg"),   appName  = o.getString("app"),
                                    senderName  = o.getString("sender"),messageText = o.getString("msg"),
                                    timestamp   = o.getLong("ts"),      isGroup  = o.getBoolean("group")
                                ))
                            }
                            withContext(Dispatchers.IO) { AppDatabase.get(ctx).messageDao().insertAll(msgs) }
                            Toast.makeText(ctx, "Imported ${msgs.size} messages", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) { Toast.makeText(ctx, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show() }
                    }
                }.setNegativeButton(R.string.cancel, null).show()
        }

        // Clear
        b.btnClear.setOnClickListener {
            AlertDialog.Builder(ctx).setTitle("Clear All").setMessage(getString(R.string.clear_confirm))
                .setPositiveButton(getString(R.string.yes_delete)) { _, _ -> vm.deleteAll(); Toast.makeText(ctx, "Cleared", Toast.LENGTH_SHORT).show() }
                .setNegativeButton(R.string.cancel, null).show()
        }

        // Feedback with device info
        b.btnFeedback.setOnClickListener {
            val info = "Device: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}\nApp: YMR v1.0"
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.dev_email)}")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "YMR Feedback")
                    putExtra(Intent.EXTRA_TEXT, "$info\n\n[Your feedback here]")
                }, "Send Feedback"))
        }

        // Contact
        b.btnContact.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.dev_email)}")).apply {
                putExtra(Intent.EXTRA_SUBJECT, "YMR Contact")
            })
        }

        // Donate
        b.btnDonate.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.kofi_url))))
        }

        // Privacy
        b.btnPrivacy.setOnClickListener {
            AlertDialog.Builder(ctx).setTitle(getString(R.string.privacy_policy))
                .setMessage(getString(R.string.privacy_text))
                .setPositiveButton("Close", null).show()
        }
    }

    override fun onResume() { super.onResume(); refreshPermDots() }

    private fun refreshPermDots() {
        val ctx = requireContext()
        val notifOk   = isNotifEnabled()
        val storageOk = if (Build.VERSION.SDK_INT >= 30) android.os.Environment.isExternalStorageManager()
                        else has(Manifest.permission.READ_EXTERNAL_STORAGE)
        val audioOk   = has(Manifest.permission.RECORD_AUDIO)
        val callOk    = has(Manifest.permission.READ_CALL_LOG)
        setDot(b.dotNotif,   notifOk);   setDot(b.dotStorage, storageOk)
        setDot(b.dotAudio,   audioOk);   setDot(b.dotCall,    callOk)
        b.btnGrantNotif.visibility   = if (notifOk)   View.GONE else View.VISIBLE
        b.btnGrantStorage.visibility = if (storageOk) View.GONE else View.VISIBLE
        b.btnGrantAudio.visibility   = if (audioOk)   View.GONE else View.VISIBLE
        b.btnGrantCall.visibility    = if (callOk)    View.GONE else View.VISIBLE
    }

    private fun setDot(v: View, ok: Boolean) =
        v.setBackgroundColor(if (ok) Color.parseColor("#006D40") else Color.parseColor("#BA1A1A"))

    private fun has(p: String) = ContextCompat.checkSelfPermission(requireContext(), p) == PackageManager.PERMISSION_GRANTED

    private fun isNotifEnabled(): Boolean {
        val flat = Settings.Secure.getString(requireContext().contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(ComponentName(requireContext(), com.mnmyounus.ymr.service.YMRNotificationListener::class.java).flattenToString())
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
