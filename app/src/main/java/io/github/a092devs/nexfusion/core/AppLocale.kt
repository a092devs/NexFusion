package io.github.a092devs.nexfusion.core

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import io.github.a092devs.nexfusion.updates.AppPreferenceNames
import io.github.a092devs.nexfusion.updates.AppSettings
import java.util.Locale

/** Applies the language chosen in Settings to a context's resources. */
object AppLocale {

    /** Returns the locale for [tag], or null when the system language is followed. */
    fun localeFor(tag: String): Locale? = when (AppPreferenceNames.normalizeLanguage(tag)) {
        AppPreferenceNames.LANGUAGE_ENGLISH -> Locale.ENGLISH
        AppPreferenceNames.LANGUAGE_SIMPLIFIED_CHINESE -> Locale.SIMPLIFIED_CHINESE
        else -> null
    }

    /** Wraps [base] so its resources follow the selected language. */
    fun wrap(base: Context): Context {
        val locale = runCatching { localeFor(AppSettings(base).language) }.getOrNull() ?: return base
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        return base.createConfigurationContext(configuration)
    }
}
