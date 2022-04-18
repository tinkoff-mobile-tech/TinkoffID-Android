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

package ru.tinkoff.core.tinkoffId

import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import ru.tinkoff.core.tinkoffId.api.TinkoffIdApi
import ru.tinkoff.core.tinkoffId.api.TinkoffIdApi.Companion.TOKEN_HINT_TYPE_ACCESS_TOKEN
import ru.tinkoff.core.tinkoffId.api.TinkoffIdApi.Companion.TOKEN_HINT_TYPE_REFRESH_TOKEN
import ru.tinkoff.core.tinkoffId.error.TinkoffErrorMessage
import ru.tinkoff.core.tinkoffId.error.TinkoffRequestException
import ru.tinkoff.core.tinkoffId.error.TinkoffTokenErrorConstants
import ru.tinkoff.core.tinkoffId.error.TokenSignOutErrorConstants
import java.io.IOException

/**
 * @author Stanislav Mukhametshin
 */
internal class TinkoffPartnerApiService(private val api: TinkoffIdApi) {

    fun getToken(code: String, codeVerifier: String, clientId: String, redirectUri: String): TinkoffCall<TinkoffTokenPayload> {
        return api.getToken(code, codeVerifier, clientId, redirectUri).wrapTokenToTinkoffCall()
    }

    fun refreshToken(refreshToken: String, clientId: String): TinkoffCall<TinkoffTokenPayload> {
        return api.refreshToken(refreshToken, clientId).wrapTokenToTinkoffCall()
    }

    fun revokeAccessToken(accessToken: String, clientId: String): TinkoffCall<Unit> {
        return api.revokeToken(accessToken, TOKEN_HINT_TYPE_ACCESS_TOKEN, clientId).wrapToTinkoffCall(
            { },
            { getRevokeErrorType(it) }
        )
    }

    fun revokeRefreshToken(refreshToken: String, clientId: String): TinkoffCall<Unit> {
        return api.revokeToken(refreshToken, TOKEN_HINT_TYPE_REFRESH_TOKEN, clientId).wrapToTinkoffCall(
            { },
            { getRevokeErrorType(it) }
        )
    }

    private fun Call.wrapTokenToTinkoffCall(): TinkoffCall<TinkoffTokenPayload> {
        return wrapToTinkoffCall({
            if (it.body == null) throw TinkoffRequestException(IOException("Empty body $it"))
            val jsonObject = JSONObject(requireNotNull(it.body).string())
            TinkoffTokenPayload(
                accessToken = jsonObject.getString("access_token"),
                expiresIn = jsonObject.getInt("expires_in"),
                idToken = jsonObject.optString("id_token"),
                refreshToken = jsonObject.getString("refresh_token")
            )
        }, { getTokenErrorType(it) })
    }

    private fun getTokenErrorType(error: String): Int {
        return when (error) {
            "invalid_request" -> TinkoffTokenErrorConstants.INVALID_REQUEST
            "invalid_client" -> TinkoffTokenErrorConstants.INVALID_CLIENT
            "invalid_grant" -> TinkoffTokenErrorConstants.INVALID_GRANT
            "unauthorized_client" -> TinkoffTokenErrorConstants.UNAUTHORIZED_CLIENT
            "unsupported_grant_type" -> TinkoffTokenErrorConstants.UNSUPPORTED_GRANT_TYPE
            "server_error" -> TinkoffTokenErrorConstants.SERVER_ERROR
            "limit_exceeded" -> TinkoffTokenErrorConstants.LIMIT_EXCEEDED
            else -> TinkoffTokenErrorConstants.UNKNOWN_ERROR
        }
    }

    private fun getRevokeErrorType(error: String): Int {
        return when (error) {
            "invalid_request" -> TokenSignOutErrorConstants.INVALID_REQUEST
            "invalid_grant" -> TokenSignOutErrorConstants.INVALID_GRANT
            else -> TokenSignOutErrorConstants.UNKNOWN_ERROR
        }
    }

    private fun <T> Call.wrapToTinkoffCall(
        responseMapping: (response: Response) -> T,
        errorMapping: (errorCode: String) -> Int
    ): TinkoffCall<T> {
        return object : TinkoffCall<T> {

            @Throws(TinkoffRequestException::class)
            @Suppress("TooGenericExceptionCaught")
            override fun getResponse(): T {
                try {
                    val response = execute()
                    response.checkResponseOnErrors(errorMapping)
                    if (!response.isSuccessful) throw IOException("Unexpected response $response")
                    return responseMapping(response)
                } catch (e: Exception) {
                    throw if (e !is TinkoffRequestException) TinkoffRequestException(reason = e) else e
                }
            }

            override fun cancel() = this@wrapToTinkoffCall.cancel()
        }
    }

    @Throws(TinkoffRequestException::class)
    private fun Response.checkResponseOnErrors(errorMapping: (errorCode: String) -> Int) {
        if (isSuccessful) return
        val message = if (body != null) {
            try {
                val jsonObject = JSONObject(body!!.string())
                val error: String? = jsonObject.optString("error")
                val message: String? = jsonObject.optString("error_message")
                if (error != null) {
                    TinkoffErrorMessage(message, errorMapping(error))
                } else null
            } catch (e: JSONException) {
                null
            }
        } else null
        throw TinkoffRequestException(
            reason = IOException("Request problem $this"),
            message = "Request exception",
            errorMessage = message
        )
    }
}
