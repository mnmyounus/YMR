package com.mnmyounus.ymr
import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mnmyounus.ymr.databinding.ActivityMainBinding
import com.mnmyounus.ymr.util.PrefsUtil

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)

        val nav = (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
        b.bottomNav.setupWithNavController(nav)

        nav.addOnDestinationChangedListener { _, dest, _ ->
            val isRoot = dest.id == R.id.homeFragment || dest.id == R.id.featuresFragment || dest.id == R.id.settingsFragment
            b.bottomNav.visibility    = if (isRoot) View.VISIBLE else View.VISIBLE
            b.appBarLayout.visibility = View.VISIBLE
        }

        b.bannerNotif.visibility = if (isNotifEnabled()) View.GONE else View.VISIBLE
        b.bannerNotif.setOnClickListener {
            startActivity(android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        requestAllPermissions()
    }

    override fun onResume() {
        super.onResume()
        b.bannerNotif.visibility = if (isNotifEnabled()) View.GONE else View.VISIBLE
    }

    private fun isNotifEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(ComponentName(this, com.mnmyounus.ymr.service.YMRNotificationListener::class.java).flattenToString())
    }

    private fun requestAllPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (!has(Manifest.permission.READ_MEDIA_IMAGES))    needed += Manifest.permission.READ_MEDIA_IMAGES
            if (!has(Manifest.permission.READ_MEDIA_VIDEO))     needed += Manifest.permission.READ_MEDIA_VIDEO
            if (!has(Manifest.permission.READ_MEDIA_AUDIO))     needed += Manifest.permission.READ_MEDIA_AUDIO
            if (!has(Manifest.permission.POST_NOTIFICATIONS))   needed += Manifest.permission.POST_NOTIFICATIONS
        } else {
            if (!has(Manifest.permission.READ_EXTERNAL_STORAGE))  needed += Manifest.permission.READ_EXTERNAL_STORAGE
            if (!has(Manifest.permission.WRITE_EXTERNAL_STORAGE)) needed += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        if (!has(Manifest.permission.READ_CALL_LOG))  needed += Manifest.permission.READ_CALL_LOG
        if (!has(Manifest.permission.READ_CONTACTS))  needed += Manifest.permission.READ_CONTACTS
        if (!has(Manifest.permission.RECORD_AUDIO))   needed += Manifest.permission.RECORD_AUDIO
        if (needed.isNotEmpty()) permLauncher.launch(needed.toTypedArray())
    }

    private fun has(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
}
