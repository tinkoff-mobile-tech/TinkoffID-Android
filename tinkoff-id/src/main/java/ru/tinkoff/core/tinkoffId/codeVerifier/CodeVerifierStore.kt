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

import android.content.Context
import android.content.SharedPreferences

/**
 * @author Stanislav Mukhametshin
 */
internal class CodeVerifierStore(context: Context) {

    private val preference: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    internal var codeVerifier: String
        get() = requireNotNull(preference.getString(CODE_VERIFIER, ""))
        set(value) = preference.edit().putString(CODE_VERIFIER, value).apply()

    companion object {

        private const val PREFS_FILENAME = "prefs_tinkoff_partner"
        private const val CODE_VERIFIER = "code_verifier"
    }
}