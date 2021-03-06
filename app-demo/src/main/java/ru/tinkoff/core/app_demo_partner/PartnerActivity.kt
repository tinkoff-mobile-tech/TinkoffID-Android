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
import kotlin.LazyThreadSafetyMode.NONE

class PartnerActivity : AppCompatActivity() {

    private val partnerUri: Uri = Uri.Builder()
        .scheme("https")
        .authority("www.partner.com")
        .appendPath("partner")
        .build()

    private lateinit var tinkoffPartnerAuth: TinkoffIdAuth

    private val partnerPresenter by lazy(NONE) { PartnerPresenter(tinkoffPartnerAuth, this) }
    private val clientIdEditText by lazy(NONE) { findViewById<EditText>(R.id.etClientId) }
    private val buttonSaveClientId by lazy(NONE) { findViewById<Button>(R.id.buttonSaveClientId) }
    private val compactButtonTinkoffAuth by lazy(NONE) { findViewById<Button>(R.id.compactButtonTinkoffAuth) }
    private val standardButtonTinkoffAuth by lazy(NONE) { findViewById<Button>(R.id.standardButtonTinkoffAuth) }
    private val buttonUpdateToken by lazy(NONE) { findViewById<Button>(R.id.buttonUpdateToken) }
    private val buttonRevokeToken by lazy(NONE) { findViewById<Button>(R.id.buttonRevokeToken) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner)
        tinkoffPartnerAuth = TinkoffIdAuth(applicationContext, "test-partner-mobile")
        intent.data?.let { partnerPresenter.getToken(it) }

        val clickListener = View.OnClickListener {
            if (tinkoffPartnerAuth.isTinkoffAuthAvailable()) {
                val intent = tinkoffPartnerAuth.createTinkoffAuthIntent(partnerUri)
                startActivity(intent)
            }
        }

        compactButtonTinkoffAuth.setOnClickListener(clickListener)
        standardButtonTinkoffAuth.setOnClickListener(clickListener)

        buttonUpdateToken.setOnClickListener {
            partnerPresenter.refreshToken()
        }
        buttonRevokeToken.setOnClickListener {
            partnerPresenter.revokeToken()
        }

        buttonSaveClientId.setOnClickListener {
            tinkoffPartnerAuth = TinkoffIdAuth(applicationContext, clientIdEditText.text.toString())
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
