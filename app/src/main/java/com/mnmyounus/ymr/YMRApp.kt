package com.mnmyounus.ymr
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mnmyounus.ymr.util.PrefsUtil

class YMRApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }
    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            if (PrefsUtil.isDarkTheme(this)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
