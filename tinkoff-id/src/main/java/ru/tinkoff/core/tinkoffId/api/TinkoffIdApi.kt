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

package ru.tinkoff.core.tinkoffId.api

import okhttp3.Call
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.ByteString.Companion.encodeUtf8
import ru.tinkoff.core.tinkoffId.BuildConfig

/**
 * @author Stanislav Mukhametshin
 */
internal class TinkoffIdApi(
    private val client: OkHttpClient,
    private val host: HttpUrl = HOST.toHttpUrl()
) {

    fun getToken(code: String, codeVerifier: String, clientId: String, redirectUri: String): Call {
        val formBody = FormBody.Builder()
            .add(FIELD_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE)
            .add(FIELD_CODE, code)
            .add(FIELD_REDIRECT_URI, redirectUri)
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
        val url = host.newBuilder()
            .addPathSegments(path)
            .build()
        val request = Request.Builder()
            .addHeader(HEADER_AUTHORIZATION, generateBasicHeader(clientId))
            .addHeader(HEADER_ACCEPT, "application/json")
            .addHeader(HEADER_X_SSO_NO_ADAPTER, "true")
            .url(url)
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
        private const val GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
        private const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"

        const val TOKEN_HINT_TYPE_ACCESS_TOKEN = "access_token"
        const val TOKEN_HINT_TYPE_REFRESH_TOKEN = "refresh_token"
    }
}
