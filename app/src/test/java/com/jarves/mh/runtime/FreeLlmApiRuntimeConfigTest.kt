package com.jarves.mh.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeLlmApiRuntimeConfigTest {
    @Test
    fun onlyEmbeddedLoopbackFreeLlmApiIsAutoManaged() {
        assertTrue(isEmbeddedFreeLlmApiEndpoint("http://127.0.0.1:3001"))
        assertTrue(isEmbeddedFreeLlmApiEndpoint("http://localhost:3001/v1"))
        assertTrue(isEmbeddedFreeLlmApiEndpoint("http://[::1]:3001/v1/"))
        assertFalse(isEmbeddedFreeLlmApiEndpoint("http://192.168.1.8:3001"))
        assertFalse(isEmbeddedFreeLlmApiEndpoint("http://localhost:31415"))
        assertFalse(isEmbeddedFreeLlmApiEndpoint("https://localhost:3001"))
        assertFalse(isEmbeddedFreeLlmApiEndpoint("http://localhost:3001/admin"))
    }

    @Test
    fun gatewayUsesLoopbackAndBuiltInNodeSqlite() {
        val environment = FreeLlmApiRuntimeConfig.serverEnvironment("a".repeat(64))
        assertEquals("127.0.0.1", environment["HOST"])
        assertEquals("3001", environment["PORT"])
        assertEquals("1", environment[FreeLlmApiRuntimeConfig.SQLITE_OVERRIDE])
        assertEquals("/workspace/data/freeapi.db", environment["FREEAPI_DB_PATH"])
        assertEquals("7", environment["REQUEST_ANALYTICS_RETENTION_DAYS"])
        assertEquals("5000", environment["REQUEST_ANALYTICS_MAX_ROWS"])
    }

    @Test
    fun serverRejectsInvalidEncryptionKey() {
        assertThrows(IllegalArgumentException::class.java) {
            FreeLlmApiRuntimeConfig.serverEnvironment("not-a-valid-key")
        }
    }
}
