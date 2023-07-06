package ru.tinkoff.core.app_demo_partner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.core.tinkoffId.TinkoffIdAuth
import ru.tinkoff.core.tinkoffId.ui.TinkoffIdSignInButton
import kotlin.LazyThreadSafetyMode.NONE

class PartnerActivity : AppCompatActivity() {

    private val partnerUri: Uri = Uri.Builder()
        .scheme("https")
        .authority("www.partner.com")
        .appendPath("partner")
        .build()

    private val redirectUri = "mobile://"
    private val clientId = "test-partner-mobile"

    private lateinit var tinkoffPartnerAuth: TinkoffIdAuth

    private val partnerPresenter by lazy(NONE) { PartnerPresenter(tinkoffPartnerAuth, this) }
    private val clientIdEditText by lazy(NONE) { findViewById<EditText>(R.id.etClientId) }
    private val redirectUriEditText by lazy(NONE) { findViewById<EditText>(R.id.etRedirectUri) }

    private val reset by lazy(NONE) { findViewById<Button>(R.id.reset) }
    private val compactBlackButtonTinkoffAuth by lazy(NONE) { findViewById<TinkoffIdSignInButton>(R.id.compactBlackButtonTinkoffAuth) }
    private val compactGrayButtonTinkoffAuth by lazy(NONE) { findViewById<TinkoffIdSignInButton>(R.id.compactGrayButtonTinkoffAuth) }
    private val compactYellowButtonTinkoffAuth by lazy(NONE) { findViewById<TinkoffIdSignInButton>(R.id.compactYellowButtonTinkoffAuth) }
    private val standardSmallBlackButtonTinkoffAuth by lazy(NONE) { findViewById<TinkoffIdSignInButton>(R.id.standardSmallBlackButtonTinkoffAuth) }
    private val standardMediumGrayButtonTinkoffAuth by lazy(NONE) { findViewById<TinkoffIdSignInButton>(R.id.standardMediumGrayButtonTinkoffAuth) }
    private val standardLargeYellowButtonTinkoffAuth by lazy(NONE) { findViewById<TinkoffIdSignInButton>(R.id.standardLargeYellowButtonTinkoffAuth) }
    private val buttonUpdateToken by lazy(NONE) { findViewById<Button>(R.id.buttonUpdateToken) }
    private val buttonRevokeToken by lazy(NONE) { findViewById<Button>(R.id.buttonRevokeToken) }

    @Suppress("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner)
        resetTinkoffIdAuth()
        intent.data?.let { partnerPresenter.getToken(it) }

        val clickListener = View.OnClickListener {
            if (isDataCorrect()) {
                initTinkoffIdAuth()
                val intent = if (tinkoffPartnerAuth.isTinkoffAppAuthAvailable()) {
                    tinkoffPartnerAuth.createTinkoffAppAuthIntent(partnerUri)
                } else {
                    tinkoffPartnerAuth.createTinkoffWebViewAuthIntent(partnerUri)
                }
                startActivity(intent)
            }
        }

        compactBlackButtonTinkoffAuth.setOnClickListener(clickListener)
        compactGrayButtonTinkoffAuth.setOnClickListener(clickListener)
        compactYellowButtonTinkoffAuth.setOnClickListener(clickListener)
        standardSmallBlackButtonTinkoffAuth.setOnClickListener(clickListener)
        standardMediumGrayButtonTinkoffAuth.setOnClickListener(clickListener)
        standardLargeYellowButtonTinkoffAuth.setOnClickListener(clickListener)

        buttonUpdateToken.setOnClickListener {
            partnerPresenter.refreshToken()
        }
        buttonRevokeToken.setOnClickListener {
            partnerPresenter.revokeToken()
        }

        reset.setOnClickListener {
            resetTinkoffIdAuth()
        }
    }

    private fun initTinkoffIdAuth() {
        val clientId = clientIdEditText.text.toString()
        val redirectUri = redirectUriEditText.text.toString()

        tinkoffPartnerAuth = TinkoffIdAuth(
            applicationContext,
            clientId = clientId,
            redirectUri = redirectUri
        )
    }

    private fun resetTinkoffIdAuth() {
        clientIdEditText.error = null
        clientIdEditText.setText(clientId)
        redirectUriEditText.error = null
        redirectUriEditText.setText(redirectUri)
        initTinkoffIdAuth()
    }

    private fun isDataCorrect(): Boolean {
        return when {
            clientIdEditText.text.isEmpty() -> {
                clientIdEditText.error = getString(R.string.partner_auth_edit_text_empty_error_description)
                false
            }
            redirectUriEditText.text.isEmpty() -> {
                redirectUriEditText.error = getString(R.string.partner_auth_edit_text_empty_error_description)
                false
            }
            else -> true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { partnerPresenter.getToken(it) }
    }

    fun onTokenAvailable() {
        buttonUpdateToken.visibility = View.VISIBLE
        buttonRevokeToken.visibility = View.VISIBLE
        Toast.makeText(this, "Token Available", Toast.LENGTH_SHORT).show()
    }

    fun onTokenRefresh() {
        Toast.makeText(this, "Token Refresh Success", Toast.LENGTH_SHORT).show()
    }

    fun onTokenRevoke() {
        Toast.makeText(this, "Token Revoke Success", Toast.LENGTH_SHORT).show()
    }

    fun onCancelledByUser() {
        Toast.makeText(this, "Partner auth was cancelled", Toast.LENGTH_SHORT).show()
    }

    fun onAuthError() {
        Toast.makeText(this, "Auth error occurred", Toast.LENGTH_SHORT).show()
    }
}
