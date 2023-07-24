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
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import ru.tinkoff.core.tinkoffId.api.TinkoffIdApi
import ru.tinkoff.core.tinkoffId.codeVerifier.CodeVerifierStore
import ru.tinkoff.core.tinkoffId.codeVerifier.CodeVerifierUtil
import ru.tinkoff.core.tinkoffId.error.TinkoffRequestException
import ru.tinkoff.core.tinkoffId.ui.webView.TinkoffWebViewUiData

/**
 * Main facade for tinkoff id authorization
 */
public class TinkoffIdAuth(
    context: Context,
    private val clientId: String,
    private val redirectUri: String
) {

    private val applicationContext = context.applicationContext
    private val partnerService: TinkoffPartnerApiService
    private val codeVerifierStore = CodeVerifierStore(applicationContext)

    init {
        val api = TinkoffIdApi.createTinkoffIdApi(context)
        partnerService = TinkoffPartnerApiService(api)
    }

    /**
     * Creates an intent to open Tinkoff App or WebView Activity for authorization via Tinkoff web
     * based on the results of the method [isTinkoffAppAuthAvailable()][isTinkoffAppAuthAvailable]
     * and later return authorization data.
     *
     * @param callbackUrl AppLink/DeepLink that will be opened when authorization process finishes
     * @return intent for authorization via Tinkoff
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public fun createTinkoffAuthIntent(callbackUrl: Uri): Intent {
        return if (isTinkoffAppAuthAvailable()) {
            createTinkoffAppAuthIntent(callbackUrl)
        } else {
            createTinkoffWebViewAuthIntent(callbackUrl)
        }
    }

    /**
     * Creates an intent to open Tinkoff App and later returns authorization data.
     *
     * @param callbackUrl AppLink/DeepLink that will be opened when authorization process finishes
     * @return implicit Intent to open Tinkoff App
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public fun createTinkoffAppAuthIntent(callbackUrl: Uri): Intent {
        return buildIntentWithPCKEState { codeChallenge, codeChallengeMethod ->
            AppLinkUtil.createTinkoffAppAuthAppLink(
                clientId,
                codeChallenge,
                codeChallengeMethod,
                callbackUrl,
                applicationContext.packageName,
                redirectUri,
                BuildConfig.VERSION_NAME,
            )
        }
    }

    /**
     * Creates an intent to open WebView Activity for authorization via Tinkoff web and later returns authorization data.
     *
     * @param callbackUrl AppLink/DeepLink that will be opened when authorization process will be finished
     * @return explicit Intent to open [TinkoffWebViewAuthActivity][ru.tinkoff.core.tinkoffId.ui.webView.TinkoffWebViewAuthActivity]
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public fun createTinkoffWebViewAuthIntent(callbackUrl: Uri): Intent {
        return buildIntentWithPCKEState { codeChallenge, codeChallengeMethod ->
            AppLinkUtil.createWebViewAuthIntent(
                context = applicationContext,
                uiData = TinkoffWebViewUiData(
                    clientId = clientId,
                    codeChallenge = codeChallenge,
                    codeChallengeMethod = codeChallengeMethod,
                    redirectUri = redirectUri,
                    callbackUrl = callbackUrl.toString()
                ),
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun buildIntentWithPCKEState(
        intentBuilder: (codeChallenge: String, codeChallengeMethod: String) -> Intent,
    ): Intent {
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
        val codeChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier)
        val codeChallengeMethod = CodeVerifierUtil.getCodeVerifierChallengeMethod()
        codeVerifierStore.codeVerifier = codeVerifier
        return intentBuilder(codeChallenge, codeChallengeMethod)
    }

    /**
     * Checks if authorization via Tinkoff App is available on current device
     *
     * @return true if we can open Tinkoff App
     */
    public fun isTinkoffAppAuthAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                AppLinkUtil.isPossibleToHandleAppLink(applicationContext) &&
                CodeVerifierUtil.getCodeVerifierChallengeMethod() != CodeVerifierUtil.CODE_CHALLENGE_METHOD_PLAIN
    }

    /**
     * Function to get Application Token Call
     *
     * @param uri the uri returned after authorization process from Tinkoff App
     * in callbackIntent based on callbackUrl from [createTinkoffAppAuthIntent]
     * @return [TinkoffCall] object to get Tinkoff Token by sending request
     *
     * @throws TinkoffRequestException if request not executed
     *
     */
    @Throws(TinkoffRequestException::class)
    public fun getTinkoffTokenPayload(uri: Uri): TinkoffCall<TinkoffTokenPayload> {
        val code = requireNotNull(AppLinkUtil.getAuthCode(uri))
        return partnerService.getToken(code, codeVerifierStore.codeVerifier, clientId, redirectUri)
    }

    /**
     * Function to get status code after authorization process
     *
     * @param uri the uri returned after authorization process from Tinkoff App
     * in callbackIntent based on callbackUrl from [createTinkoffAppAuthIntent]
     * @return [TinkoffIdStatusCode][ru.tinkoff.core.tinkoffId.TinkoffIdStatusCode].
     * SUCCESS - we can perform getTinkoffTokenPayload(), CANCELLED_BY_USER -  user
     * canceled authorization process
     *
     */
    public fun getStatusCode(uri: Uri): TinkoffIdStatusCode? {
        return AppLinkUtil.getAuthStatusCode(uri)
    }

    /**
     * Function to get Application Refresh Token Call
     *
     * @param refreshToken [refreshToken][TinkoffTokenPayload.refreshToken] of current session
     * @return [TinkoffCall] object to get Tinkoff Token by sending request
     *
     * @throws TinkoffRequestException if request not executed
     */
    @Throws(TinkoffRequestException::class)
    public fun obtainTokenPayload(refreshToken: String): TinkoffCall<TinkoffTokenPayload> {
        return partnerService.refreshToken(refreshToken, clientId)
    }

    /**
     * Sign out call by using accessToken
     *
     * @param accessToken [accessToken][TinkoffTokenPayload.accessToken] of current session
     * @return [TinkoffCall] object that will return Unit if request successfully executed
     *
     * @throws TinkoffRequestException if request not executed
     */
    @Throws(TinkoffRequestException::class)
    public fun signOutByAccessToken(accessToken: String): TinkoffCall<Unit> {
        return partnerService.revokeAccessToken(accessToken, clientId)
    }

    /**
     * Sign out call by using refreshToken
     *
     * @param refreshToken [refreshToken][TinkoffTokenPayload.refreshToken] of current session
     * @return [TinkoffCall] object that will return Unit if request successfully executed
     *
     * @throws TinkoffRequestException if request not executed
     */
    @Throws(TinkoffRequestException::class)
    public fun signOutByRefreshToken(refreshToken: String): TinkoffCall<Unit> {
        return partnerService.revokeRefreshToken(refreshToken, clientId)
    }
}
