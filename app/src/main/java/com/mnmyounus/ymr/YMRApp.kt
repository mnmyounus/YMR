package com.mnmyounus.ymr
import android.app.Application
import android.content.res.Configuration
import com.mnmyounus.ymr.util.PrefsUtil

class YMRApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }
    fun applyTheme() {
        val isDark = PrefsUtil.isDarkTheme(this)
        android.app.UiModeManager
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
