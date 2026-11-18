package com.jarves.mh.ui.theme

/** The app intentionally ships English resources only. */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
) {
    SYSTEM("system", "System Default", "Default"),
    ENGLISH("en", "English", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            if (code.equals("en", ignoreCase = true)) ENGLISH else SYSTEM
    }
}
