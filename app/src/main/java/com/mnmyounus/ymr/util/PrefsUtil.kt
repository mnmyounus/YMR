package com.mnmyounus.ymr.util
import android.content.Context

object PrefsUtil {
    private const val P = "ymr_prefs"
    private fun p(ctx: Context) = ctx.getSharedPreferences(P, Context.MODE_PRIVATE)

    fun isSvcEnabled(ctx: Context) = p(ctx).getBoolean("svc", true)
    fun setSvcEnabled(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("svc", v).apply()
    fun isDarkTheme(ctx: Context) = p(ctx).getBoolean("dark", false)
    fun setDarkTheme(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("dark", v).apply()
    fun getFilter(ctx: Context): String = p(ctx).getString("filter", "") ?: ""
    fun setFilter(ctx: Context, f: String) = p(ctx).edit().putString("filter", f).apply()
}
