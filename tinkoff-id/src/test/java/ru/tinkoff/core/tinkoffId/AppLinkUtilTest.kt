package ru.tinkoff.core.tinkoffId

import android.content.Intent
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * @author k.voskrebentsev
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
internal class AppLinkUtilTest {

    @Test
    fun testWebViewIntentParsing() {
        val intent = Intent()
            .putExtra(QUERY_PARAMETER_CLIENT_ID, VALUE_PARAMETER_CLIENT_ID)
            .putExtra(QUERY_PARAMETER_CODE_CHALLENGE, VALUE_PARAMETER_CODE_CHALLENGE)
            .putExtra(QUERY_PARAMETER_CODE_CHALLENGE_METHOD, VALUE_PARAMETER_CODE_CHALLENGE_METHOD)
            .putExtra(QUERY_PARAMETER_REDIRECT_URI, VALUE_PARAMETER_REDIRECT_URI)
            .putExtra(QUERY_PARAMETER_CALLBACK_URL, VALUE_PARAMETER_CALLBACK_URL)

        val result = AppLinkUtil.parseTinkoffWebViewUiData(intent)

        assertEquals(VALUE_PARAMETER_CLIENT_ID, result.clientId)
        assertEquals(VALUE_PARAMETER_CODE_CHALLENGE, result.codeChallenge)
        assertEquals(VALUE_PARAMETER_CODE_CHALLENGE_METHOD, result.codeChallengeMethod)
        assertEquals(VALUE_PARAMETER_REDIRECT_URI, result.redirectUri)
        assertEquals(VALUE_PARAMETER_CALLBACK_URL, result.callbackUrl)
    }

    @Test
    fun testCodeIntentCreation() {
        val code = "code_value"

        val result = AppLinkUtil.createBackAppCodeIntent(
            callbackUrl = VALUE_PARAMETER_CALLBACK_URL,
            code = code,
        )

        assertEquals(Intent.ACTION_VIEW, result.action)
        assertEquals(VALUE_PARAMETER_CALLBACK_URL, result.data?.path)
        assertEquals(code, result.data?.getQueryParameter(QUERY_PARAMETER_CODE))
        assertEquals(AUTH_STATUS_CODE_SUCCESS, result.data?.getQueryParameter(QUERY_PARAMETER_AUTH_STATUS_CODE))
    }

    @Test
    fun testCancelIntentCreation() {
        val result = AppLinkUtil.createBackAppCancelIntent(
            callbackUrl = VALUE_PARAMETER_CALLBACK_URL,
        )

        assertEquals(Intent.ACTION_VIEW, result.action)
        assertEquals(VALUE_PARAMETER_CALLBACK_URL, result.data?.path)
        assertEquals(AUTH_STATUS_CODE_CANCELLED_BY_USER, result.data?.getQueryParameter(QUERY_PARAMETER_AUTH_STATUS_CODE))
    }

    companion object {
        private const val QUERY_PARAMETER_CLIENT_ID = "clientId"
        private const val QUERY_PARAMETER_CODE_CHALLENGE = "code_challenge"
        private const val QUERY_PARAMETER_CODE_CHALLENGE_METHOD = "code_challenge_method"
        private const val QUERY_PARAMETER_CALLBACK_URL = "callback_url"
        private const val QUERY_PARAMETER_REDIRECT_URI = "redirect_uri"

        private const val VALUE_PARAMETER_CLIENT_ID = "clientId_value"
        private const val VALUE_PARAMETER_CODE_CHALLENGE = "code_challenge_value"
        private const val VALUE_PARAMETER_CODE_CHALLENGE_METHOD = "code_challenge_method_value"
        private const val VALUE_PARAMETER_CALLBACK_URL = "callback_url_value"
        private const val VALUE_PARAMETER_REDIRECT_URI = "redirect_uri_value"

        private const val QUERY_PARAMETER_CODE = "code"
        private const val QUERY_PARAMETER_AUTH_STATUS_CODE = "auth_status_code"

        private const val AUTH_STATUS_CODE_SUCCESS = "success"
        private const val AUTH_STATUS_CODE_CANCELLED_BY_USER = "cancelled_by_user"
    }
}
