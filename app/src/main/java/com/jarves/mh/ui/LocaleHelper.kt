package com.jarves.mh.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocalizedContext(
    base: Context,
    private val configContext: Context,
) : ContextWrapper(base) {
    override fun getResources(): Resources = configContext.resources
    override fun getAssets(): AssetManager = configContext.assets
}

object LocaleHelper {

    @Suppress("UNUSED_PARAMETER")
    fun getLocale(languageCode: String): Locale = Locale.ENGLISH

    @Suppress("UNUSED_PARAMETER")
    fun applyLanguage(context: Context, languageCode: String): Context {
        val targetLocale = Locale.ENGLISH
        Locale.setDefault(targetLocale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(targetLocale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = targetLocale
        }
        config.setLayoutDirection(targetLocale)
        val configContext = context.createConfigurationContext(config)
        return LocalizedContext(context, configContext)
    }
}

/** Resolves all app strings using the fixed English locale. */
object AppStrings {
    @Suppress("UNUSED_PARAMETER")
    fun languageCode(context: Context): String = "en"

    fun context(context: Context): Context = LocaleHelper.applyLanguage(context, "en")

    fun get(context: Context, @androidx.annotation.StringRes id: Int, vararg args: Any): String =
        context(context).getString(id, *args)
}
