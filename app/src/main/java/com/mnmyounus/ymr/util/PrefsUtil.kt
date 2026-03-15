package com.mnmyounus.ymr.util
import android.content.Context

object PrefsUtil {
    private const val P = "ymr_prefs"
    private fun p(ctx: Context) = ctx.getSharedPreferences(P, Context.MODE_PRIVATE)
    fun isSetup(ctx: Context) = p(ctx).getBoolean("setup", false)
    fun setPassword(ctx: Context, pw: String) {
        p(ctx).edit().putString("pw_hash", CryptoUtil.hash(pw))
            .putString("pw_plain", pw).putBoolean("setup", true).apply()
    }
    fun verify(ctx: Context, pw: String) = p(ctx).getString("pw_hash", null) == CryptoUtil.hash(pw)
    fun getServicePw(ctx: Context): String? = p(ctx).getString("pw_plain", null)
    fun isSvcEnabled(ctx: Context) = p(ctx).getBoolean("svc", true)
    fun setSvcEnabled(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("svc", v).apply()
    fun isDarkTheme(ctx: Context) = p(ctx).getBoolean("dark", false)
    fun setDarkTheme(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("dark", v).apply()
    fun getIconIndex(ctx: Context) = p(ctx).getInt("icon_idx", 0)
    fun setIconIndex(ctx: Context, i: Int) = p(ctx).edit().putInt("icon_idx", i).apply()
    fun getFilter(ctx: Context) = p(ctx).getString("filter", "") ?: ""
    fun setFilter(ctx: Context, f: String) = p(ctx).edit().putString("filter", f).apply()
}
