package com.jarves.mh.runtime

internal object FreeLlmApiRuntimeConfig {
    const val PINNED_COMMIT = "ca0a2741bad71fe6ca8b4f0e09ae6b918937d2fe"
    const val API_URL = "http://127.0.0.1:3001"
    const val SQLITE_OVERRIDE = "FREEAPI_USE_NODE_SQLITE"
    const val ENCRYPTION_KEY_STORAGE_ID = "freellmapi_server_encryption_key"

    fun serverEnvironment(encryptionKey: String): Map<String, String> {
        require(encryptionKey.matches(Regex("[0-9a-fA-F]{64}"))) {
            "FreeLLMAPI encryption key must be 32 bytes encoded as 64 hexadecimal characters"
        }
        return mapOf(
            "NODE_ENV" to "production",
            "HOST" to "127.0.0.1",
            "PORT" to "3001",
            "ENCRYPTION_KEY" to encryptionKey,
            SQLITE_OVERRIDE to "1",
            "FREEAPI_DB_PATH" to "/workspace/data/freeapi.db",
            "FREEAPI_DB_DIR_HARDENING" to "1",
            "CLIENT_DIST" to "/workspace/freellmapi/client/dist",
            "REQUEST_ANALYTICS_RETENTION_DAYS" to "7",
            "REQUEST_ANALYTICS_MAX_ROWS" to "5000",
            "QUOTA_OBSERVATIONS_RETENTION_DAYS" to "7",
            "QUOTA_OBSERVATIONS_MAX_ROWS" to "5000",
            "NODE_OPTIONS" to "--max-old-space-size=768",
        )
    }
}
