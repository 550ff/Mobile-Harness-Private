package com.jarves.mh

import com.jarves.mh.ui.LocaleHelper
import com.jarves.mh.ui.theme.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocaleHelperTest {

    @Test
    fun onlyEnglishIsSelectable() {
        assertEquals(listOf(AppLanguage.SYSTEM, AppLanguage.ENGLISH), AppLanguage.entries)
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode("system"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode("es"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode("hi"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode(null))
    }

    @Test
    fun localeHelperKeepsEnglishAndSystemFallback() {
        assertNull(LocaleHelper.getLocale("system"))
        assertNull(LocaleHelper.getLocale(""))
        assertEquals("en", LocaleHelper.getLocale("en")?.language)
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
