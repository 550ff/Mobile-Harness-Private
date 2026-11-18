package com.jarves.mh

import com.jarves.mh.ui.LocaleHelper
import com.jarves.mh.ui.theme.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocaleHelperTest {

    @Test
    fun englishIsTheOnlySelectableLanguage() {
        assertEquals(listOf(AppLanguage.ENGLISH), AppLanguage.entries)
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("system"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("es"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("hi"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(null))
    }

    @Test
    fun localeHelperAlwaysUsesEnglish() {
        assertEquals("en", LocaleHelper.getLocale("system").language)
        assertEquals("en", LocaleHelper.getLocale("").language)
        assertEquals("en", LocaleHelper.getLocale("zh").language)
        assertEquals("en", LocaleHelper.getLocale("en").language)
    }

    @Test
    fun appLanguagePropertiesArePresent() {
        for (lang in AppLanguage.entries) {
            assertTrue("Display name should not be blank for ${lang.name}", lang.displayName.isNotBlank())
            assertTrue("Native name should not be blank for ${lang.name}", lang.nativeName.isNotBlank())
            assertTrue("Code should not be blank for ${lang.name}", lang.code.isNotBlank())
        }
    }
}
