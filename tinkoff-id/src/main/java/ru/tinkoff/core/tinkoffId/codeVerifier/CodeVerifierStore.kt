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