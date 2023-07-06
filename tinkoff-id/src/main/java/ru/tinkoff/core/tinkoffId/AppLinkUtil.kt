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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import ru.tinkoff.core.tinkoffId.ui.webView.TinkoffWebViewAuthActivity
import ru.tinkoff.core.tinkoffId.ui.webView.TinkoffWebViewUiData

/**
 * @author Stanislav Mukhametshin
 */
internal object AppLinkUtil {

    private const val PARTNER_AUTH_CATEGORY = "ru.tinkoff.partner.TINKOFF_APP"
    private const val QUERY_PARAMETER_CLIENT_ID = "clientId"
    private const val QUERY_PARAMETER_CODE_CHALLENGE = "code_challenge"
    private const val QUERY_PARAMETER_CODE_CHALLENGE_METHOD = "code_challenge_method"
    private const val QUERY_PARAMETER_CALLBACK_URL = "callback_url"
    private const val QUERY_PARAMETER_PACKAGE = "package_name"
    private const val QUERY_PARAMETER_CODE = "code"
    private const val QUERY_PARAMETER_AUTH_STATUS_CODE = "auth_status_code"
    private const val QUERY_PARAMETER_REDIRECT_URI = "redirect_uri"
    private const val QUERY_PARTNER_SDK_VERSION = "partner_sdk_version"

    private const val AUTH_STATUS_CODE_SUCCESS = "success"
    private const val AUTH_STATUS_CODE_CANCELLED_BY_USER = "cancelled_by_user"

    private val baseUri = Uri.Builder()
        .scheme("https")
        .authority("www.tinkoff.ru")
        .appendPath("partner_auth")
        .build()

    fun createTinkoffAppAuthAppLink(
        clientId: String,
        codeChallenge: String,
        codeChallengeMethod: String,
        callbackUrl: Uri,
        packageName: String?,
        redirectUrl: String,
        partnerSdkVersion: String,
    ): Intent {
        val uri = baseUri.buildUpon()
            .appendQueryParameter(QUERY_PARAMETER_CLIENT_ID, clientId)
            .appendQueryParameter(QUERY_PARAMETER_CODE_CHALLENGE, codeChallenge)
            .appendQueryParameter(QUERY_PARAMETER_CODE_CHALLENGE_METHOD, codeChallengeMethod)
            .appendQueryParameter(QUERY_PARAMETER_CALLBACK_URL, callbackUrl.toString())
            .appendQueryParameter(QUERY_PARAMETER_PACKAGE, packageName)
            .appendQueryParameter(QUERY_PARAMETER_REDIRECT_URI, redirectUrl)
            .appendQueryParameter(QUERY_PARTNER_SDK_VERSION, partnerSdkVersion)
            .build()
        return Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }
    }

    fun createWebViewAuthIntent(
        context: Context,
        uiData: TinkoffWebViewUiData,
    ): Intent {
        return Intent(context, TinkoffWebViewAuthActivity::class.java)
            .putExtra(QUERY_PARAMETER_CLIENT_ID, uiData.clientId)
            .putExtra(QUERY_PARAMETER_CODE_CHALLENGE, uiData.codeChallenge)
            .putExtra(QUERY_PARAMETER_CODE_CHALLENGE_METHOD, uiData.codeChallengeMethod)
            .putExtra(QUERY_PARAMETER_REDIRECT_URI, uiData.redirectUri)
            .putExtra(QUERY_PARAMETER_CALLBACK_URL, uiData.callbackUrl)
    }

    fun parseTinkoffWebViewUiData(
        intent: Intent,
    ): TinkoffWebViewUiData {
        return TinkoffWebViewUiData(
            clientId = intent.requireStringExtra(QUERY_PARAMETER_CLIENT_ID),
            codeChallenge = intent.requireStringExtra(QUERY_PARAMETER_CODE_CHALLENGE),
            codeChallengeMethod = intent.requireStringExtra(QUERY_PARAMETER_CODE_CHALLENGE_METHOD),
            redirectUri = intent.requireStringExtra(QUERY_PARAMETER_REDIRECT_URI),
            callbackUrl = intent.requireStringExtra(QUERY_PARAMETER_CALLBACK_URL),
        )
    }

    private fun Intent.requireStringExtra(key: String): String {
        return requireNotNull(getStringExtra(key))
    }

    fun isPossibleToHandleAppLink(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(PARTNER_AUTH_CATEGORY)
            data = baseUri
        }
        val flag = PackageManager.MATCH_DEFAULT_ONLY
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(
                    flag.toLong()
                )
            )
        } else {
            context.packageManager.queryIntentActivities(intent, flag)
        }
        return list.size > 0
    }

    fun createBackAppCodeIntent(
        callbackUrl: String,
        code: String,
    ): Intent {
        val data = buildBackAppLinkWithStatus(callbackUrl, AUTH_STATUS_CODE_SUCCESS)
            .appendQueryParameter(QUERY_PARAMETER_CODE, code)
            .build()
        return Intent(Intent.ACTION_VIEW)
            .setData(data)
    }

    fun createBackAppCancelIntent(
        callbackUrl: String,
    ): Intent {
        val data = buildBackAppLinkWithStatus(callbackUrl, AUTH_STATUS_CODE_CANCELLED_BY_USER)
            .build()
        return Intent(Intent.ACTION_VIEW)
            .setData(data)
    }

    private fun buildBackAppLinkWithStatus(callbackUrl: String, status: String): Uri.Builder {
        return Uri.parse(callbackUrl).buildUpon()
            .appendQueryParameter(QUERY_PARAMETER_AUTH_STATUS_CODE, status)
    }

    fun getAuthCode(uri: Uri): String? = uri.getQueryParameter(QUERY_PARAMETER_CODE)

    fun getAuthStatusCode(uri: Uri): TinkoffIdStatusCode? {
        val statusCode = uri.getQueryParameter(QUERY_PARAMETER_AUTH_STATUS_CODE)
        return authStatusCodesMap[statusCode]
    }

    private val authStatusCodesMap = mapOf(
        AUTH_STATUS_CODE_SUCCESS to TinkoffIdStatusCode.SUCCESS,
        AUTH_STATUS_CODE_CANCELLED_BY_USER to TinkoffIdStatusCode.CANCELLED_BY_USER
    )
}
