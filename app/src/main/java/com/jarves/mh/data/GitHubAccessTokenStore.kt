package com.jarves.mh.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the private-release read token outside GitHub CLI's plaintext credential file.
 * The token is deliberately excluded from portable app backups.
 */
class GitHubAccessTokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("github_update_auth", Context.MODE_PRIVATE)

    @Synchronized
    fun save(token: String) {
        val clean = token.trim()
        require(clean.isNotBlank()) { "GitHub access token cannot be empty." }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(clean.toByteArray(Charsets.UTF_8))
        check(
            preferences.edit()
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString(KEY_VALUE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .commit(),
        ) { "Could not persist the encrypted GitHub update credential." }
    }

    @Synchronized
    fun read(): String? = runCatching {
        val ivValue = preferences.getString(KEY_IV, null) ?: return null
        val encryptedValue = preferences.getString(KEY_VALUE, null) ?: return null
        val iv = Base64.decode(ivValue, Base64.NO_WRAP)
        val encrypted = Base64.decode(encryptedValue, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        cipher.doFinal(encrypted).toString(Charsets.UTF_8).takeIf(String::isNotBlank)
    }.getOrNull()

    @Synchronized
    fun clear() {
        preferences.edit().remove(KEY_IV).remove(KEY_VALUE).commit()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val KEY_ALIAS = "mobile-harness-github-update-token-v1"
        const val KEY_IV = "token_iv"
        const val KEY_VALUE = "token_ciphertext"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
