package com.jarves.mh.ui.theme

/** Mobile Harness ships English UI resources only. */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
) {
    ENGLISH("en", "English", "English");

    companion object {
        fun fromCode(@Suppress("UNUSED_PARAMETER") code: String?): AppLanguage = ENGLISH
    }
}
