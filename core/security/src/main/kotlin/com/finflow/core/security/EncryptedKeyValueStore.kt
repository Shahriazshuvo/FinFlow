package com.finflow.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small string store whose values are encrypted with a key that never leaves the device's
 * hardware-backed keystore (APP_SPEC.md §31).
 *
 * This exists because the auth refresh token is a bearer credential: anything holding it can
 * mint access tokens until it is revoked. Supabase's default session manager writes it to
 * plain `SharedPreferences`, which is readable on a rooted or backed-up device. Here the key
 * material lives in `AndroidKeyStore` and is never extractable — the app can ask the keystore
 * to encrypt and decrypt, but cannot read the key itself.
 *
 * Deliberately not `EncryptedSharedPreferences`: `androidx.security-crypto` is deprecated, and
 * this is a small enough surface to own directly rather than take a dead dependency for.
 *
 * AES-GCM is authenticated, so a tampered value fails to decrypt rather than returning
 * plausible garbage. A fresh random IV is generated per write and stored alongside the
 * ciphertext — reusing an IV with GCM is catastrophic, so it is never derived or cached.
 */
@Singleton
class EncryptedKeyValueStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val preferences by lazy {
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    fun put(key: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey())
        }
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val packed = cipher.iv + ciphertext
        preferences.edit()
            .putString(key, Base64.encodeToString(packed, Base64.NO_WRAP))
            .apply()
    }

    /**
     * Returns `null` when the value is absent, or when it can no longer be decrypted — which
     * happens legitimately after a device restore or a biometric re-enrolment invalidates the
     * key. The stale entry is dropped so the caller simply sees "no session" and signs in
     * again, rather than the app failing on every launch.
     */
    fun get(key: String): String? {
        val stored = preferences.getString(key, null) ?: return null
        return try {
            val packed = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    secretKey(),
                    GCMParameterSpec(GCM_TAG_BITS, packed, 0, GCM_IV_BYTES),
                )
            }
            val plaintext = cipher.doFinal(packed, GCM_IV_BYTES, packed.size - GCM_IV_BYTES)
            String(plaintext, Charsets.UTF_8)
        } catch (_: GeneralSecurityException) {
            remove(key)
            null
        } catch (_: IllegalArgumentException) {
            remove(key)
            null
        }
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun secretKey(): SecretKey {
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                // No setUserAuthenticationRequired: the session must survive an app restart
                // without a prompt. Requiring auth here would gate every silent token refresh
                // behind a biometric, which is a different feature.
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "finflow.secure_store"
        const val STORE_NAME = "finflow_secure"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}
