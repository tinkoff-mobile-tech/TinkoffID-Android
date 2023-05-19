/*
 * Copyright © 2021 Tinkoff Bank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.core.tinkoffId.codeVerifier

import android.annotation.SuppressLint
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

/**
 * Generates code verifiers and challenges for PKCE exchange.
 *
 * @see "Proof Key for Code Exchange by OAuth Public Clients"
 * @author Stanislav Mukhametshin
 */
internal object CodeVerifierUtil {

    /**
     * SHA-256 based code verifier challenge method.
     *
     * @see "Proof Key for Code Exchange by OAuth Public Clients"
     */
    const val CODE_CHALLENGE_METHOD_S256 = "S256"

    /**
     * Plain-text code verifier challenge method. This is only used by AppAuth for Android if
     * SHA-256 is not supported on this platform.
     *
     * @see "Proof Key for Code Exchange by OAuth Public Clients"
     */
    const val CODE_CHALLENGE_METHOD_PLAIN = "plain"

    /**
     * The default entropy (in bytes) used for the code verifier.
     */
    private const val DEFAULT_CODE_VERIFIER_ENTROPY = 64

    /**
     * Base64 encoding settings used for generated code verifiers.
     */
    private const val PKCE_BASE64_ENCODE_SETTINGS =
        Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE

    /**
     * Generates a random code verifier string using the provided entropy source and the specified
     * number of bytes of entropy.
     */
    @SuppressLint("TrulyRandom")
    @RequiresApi(Build.VERSION_CODES.M)
    fun generateRandomCodeVerifier(): String {
        val entropySource = SecureRandom()
        val randomBytes = ByteArray(DEFAULT_CODE_VERIFIER_ENTROPY)
        entropySource.nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, PKCE_BASE64_ENCODE_SETTINGS)
    }

    /**
     * Produces a challenge from a code verifier, using SHA-256 as the challenge method if the
     * system supports it (all Android devices _should_ support SHA-256), and falls back
     * to the [&quot;plain&quot; challenge type][CODE_CHALLENGE_METHOD_PLAIN] if
     * unavailable.
     */
    fun deriveCodeVerifierChallenge(codeVerifier: String): String {
        return try {
            val sha256Digester = MessageDigest.getInstance("SHA-256")
            sha256Digester.update(codeVerifier.toByteArray(charset("ISO_8859_1")))
            val digestBytes = sha256Digester.digest()
            Base64.encodeToString(digestBytes, PKCE_BASE64_ENCODE_SETTINGS)
        } catch (e: NoSuchAlgorithmException) {
            codeVerifier
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("ISO-8859-1 encoding not supported", e)
        }
    }

    /**
     * Returns the challenge method utilized on this system: typically
     * [SHA-256][CODE_CHALLENGE_METHOD_S256] if supported by
     * the system, [plain][CODE_CHALLENGE_METHOD_PLAIN] otherwise.
     */
    // no exception, so SHA-256 is supported
    fun getCodeVerifierChallengeMethod(): String {
        return try {
            MessageDigest.getInstance("SHA-256")
            CODE_CHALLENGE_METHOD_S256
        } catch (e: NoSuchAlgorithmException) {
            CODE_CHALLENGE_METHOD_PLAIN
        }
    }
}
