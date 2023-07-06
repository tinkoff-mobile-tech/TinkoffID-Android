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
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * @author Stanislav Mukhametshin
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
public class TinkoffIdAuthTest {

    private var context: Context = ApplicationProvider.getApplicationContext()
    private val tinkoffAuth: TinkoffIdAuth = TinkoffIdAuth(context, CLIENT_ID, REDIRECT_URL)
    private val testUri: Uri = Uri.Builder()
        .scheme("https")
        .authority("www.partner.com")
        .appendPath("partner")
        .build()

    @Test
    public fun testIntentCreation() {
        val intent = tinkoffAuth.createTinkoffAppAuthIntent(testUri)
        assertThat(intent.data).isNotNull()
        val queryParam = { paramName: String -> requireNotNull(intent.data).getQueryParameter(paramName) }
        assertThat(queryParam("clientId")).isEqualTo(CLIENT_ID)
        assertThat(queryParam("code_challenge")).isNotEmpty()
        assertThat(queryParam("code_challenge_method")).isNotEmpty()
        assertThat(queryParam("redirect_uri")).isEqualTo(REDIRECT_URL)
        assertThat(queryParam("callback_url")).isEqualTo(testUri.toString())
        assertThat(queryParam("package_name")).isNotEmpty()
        assertThat(queryParam("partner_sdk_version")).isEqualTo(BuildConfig.VERSION_NAME)
    }

    @Test
    public fun testWebViewIntentCreation() {
        val intent = tinkoffAuth.createTinkoffWebViewAuthIntent(testUri)
        assertThat(intent.extras).isNotNull()
        val queryParam = { paramName: String -> requireNotNull(intent.getStringExtra(paramName)) }
        assertThat(queryParam("clientId")).isEqualTo(CLIENT_ID)
        assertThat(queryParam("code_challenge")).isNotEmpty()
        assertThat(queryParam("code_challenge_method")).isNotEmpty()
        assertThat(queryParam("redirect_uri")).isEqualTo(REDIRECT_URL)
        assertThat(queryParam("callback_url")).isEqualTo(testUri.toString())
    }

    @Test
    public fun testStatusWhenReturningBack() {
        listOf(AUTH_STATUS_CODE_SUCCESS to TinkoffIdStatusCode.SUCCESS, AUTH_STATUS_CODE_CANCELLED_BY_USER to TinkoffIdStatusCode.CANCELLED_BY_USER)
            .forEach { (param, status) ->
                val uri = testUri.buildUpon().appendQueryParameter(QUERY_PARAMETER_AUTH_STATUS_CODE, param).build()
                assertThat(tinkoffAuth.getStatusCode(uri)).isEqualTo(status)
            }
    }

    private companion object {

        private const val CLIENT_ID = "testClientId"
        private const val REDIRECT_URL = "mobile://redirectUrl"
        private const val QUERY_PARAMETER_AUTH_STATUS_CODE = "auth_status_code"

        private const val AUTH_STATUS_CODE_SUCCESS = "success"
        private const val AUTH_STATUS_CODE_CANCELLED_BY_USER = "cancelled_by_user"
    }
}
