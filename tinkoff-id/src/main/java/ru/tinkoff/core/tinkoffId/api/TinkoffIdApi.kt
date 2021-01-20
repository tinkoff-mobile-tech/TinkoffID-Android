package ru.tinkoff.core.tinkoffId.api

import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.ByteString.Companion.encodeUtf8
import ru.tinkoff.core.tinkoffId.BuildConfig

/**
 * @author Stanislav Mukhametshin
 */
internal class TinkoffIdApi(private val client: OkHttpClient) {

    fun getToken(code: String, codeVerifier: String, clientId: String): Call {
        val formBody = FormBody.Builder()
            .add(FIELD_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE)
            .add(FIELD_CODE, code)
            .add(FIELD_REDIRECT_URI, PARAM_DEFAULT_REDIRECT_URI)
            .add(FIELD_VENDOR, PARAM_DEFAULT_VENDOR)
            .add(FIELD_CODE_VERIFIER, codeVerifier)
            .add(FIELD_CLIENT_ID, clientId)
            .add(FIELD_CLIENT_VERSION, BuildConfig.VERSION_NAME)
            .build()

        return createRequest(PATH_TOKEN, formBody, clientId)
    }

    fun refreshToken(refreshToken: String, clientId: String): Call {
        val formBody = FormBody.Builder()
            .add(FIELD_GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN)
            .add(FIELD_REFRESH_TOKEN, refreshToken)
            .add(FIELD_VENDOR, PARAM_DEFAULT_VENDOR)
            .add(FIELD_CLIENT_ID, clientId)
            .add(FIELD_CLIENT_VERSION, BuildConfig.VERSION_NAME)
            .build()

        return createRequest(PATH_TOKEN, formBody, clientId)
    }

    fun revokeToken(token: String, tokenHint: String, clientId: String): Call {
        val formBody = FormBody.Builder()
            .add(FIELD_TOKEN, token)
            .add(FIELD_TOKEN_TYPE_HINT, tokenHint)
            .build()
        return createRequest(PATH_REVOKE, formBody, clientId)
    }

    private fun createRequest(path: String, requestBody: RequestBody, clientId: String): Call {
        val request = Request.Builder()
            .addHeader(HEADER_AUTHORIZATION, generateBasicHeader(clientId))
            .addHeader(HEADER_ACCEPT, "application/json")
            .addHeader(HEADER_X_SSO_NO_ADAPTER, "true")
            .url("$HOST$path")
            .post(requestBody)
            .build()
        return client.newCall(request)
    }

    private fun generateBasicHeader(clientId: String): String {
        val encodedClientId = "$clientId:".encodeUtf8().base64()
        return "Basic $encodedClientId"
    }

    companion object {

        private const val HOST = "https://id.tinkoff.ru"
        private const val PATH_TOKEN = "auth/token"
        private const val PATH_REVOKE = "auth/revoke"

        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_ACCEPT = "Accept"
        private const val HEADER_X_SSO_NO_ADAPTER = "X-SSO-No-Adapter"

        private const val FIELD_GRANT_TYPE = "grant_type"
        private const val FIELD_REFRESH_TOKEN = "refresh_token"
        private const val FIELD_VENDOR = "vendor"
        private const val FIELD_CLIENT_ID = "client_id"
        private const val FIELD_CODE = "code"
        private const val FIELD_REDIRECT_URI = "redirect_uri"
        private const val FIELD_CODE_VERIFIER = "code_verifier"
        private const val FIELD_TOKEN = "token"
        private const val FIELD_TOKEN_TYPE_HINT = "token_type_hint"
        private const val FIELD_CLIENT_VERSION = "client_version"

        private const val PARAM_DEFAULT_VENDOR = "tinkoff_android"
        private const val PARAM_DEFAULT_REDIRECT_URI = "mobile://"
        private const val GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
        private const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"

        const val TOKEN_HINT_TYPE_ACCESS_TOKEN = "access_token"
        const val TOKEN_HINT_TYPE_REFRESH_TOKEN = "refresh_token"
    }
}
