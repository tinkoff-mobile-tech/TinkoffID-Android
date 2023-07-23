package ru.tinkoff.core.tinkoffId.ui.webView

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ru.tinkoff.core.tinkoffId.AppLinkUtil
import ru.tinkoff.core.tinkoffId.R

/**
 * @author k.voskrebentsev
 */
internal class TinkoffWebViewAuthActivity : AppCompatActivity() {

    private val presenter: TinkoffWebViewAuthPresenter by lazy { TinkoffWebViewAuthPresenter() }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tinkoff_id_web_view_activity)

        val uiData = AppLinkUtil.parseTinkoffWebViewUiData(intent)

        initWebView(uiData)
        initToolbar(uiData)
        initBackPress(uiData)
    }

    private fun initToolbar(uiData: TinkoffWebViewUiData) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.inflateMenu(R.menu.tinkoff_id_web_view_auth_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.reloadMenuItem) {
                webView.reload()
                true
            } else {
                false
            }
        }

        toolbar.setNavigationOnClickListener {
            finishWithCancellation(uiData.callbackUrl)
        }
    }

    private fun initBackPress(uiData: TinkoffWebViewUiData) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithCancellation(uiData.callbackUrl)
            }
        }
        onBackPressedDispatcher.addCallback(callback)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(uiData: TinkoffWebViewUiData) {
        webView = findViewById(R.id.webView)
        val url = presenter.buildWebViewAuthStartUrl(uiData)
        webView.run {
            webViewClient = TinkoffWebViewClient(createTinkoffWebViewCallback(uiData))
            with(settings) {
                javaScriptEnabled = true
                setGeolocationEnabled(false)
                cacheMode = LOAD_NO_CACHE
                allowFileAccess = false
                allowContentAccess = false
            }
            loadUrl(url)
        }
    }

    private fun createTinkoffWebViewCallback(uiData: TinkoffWebViewUiData): TinkoffWebViewListener {
        return object : TinkoffWebViewListener {

            override fun isUrlForAuthCompletion(url: String): Boolean {
                return url.startsWith(uiData.redirectUri)
            }

            override fun completeAuthWithSuccess(url: String) {
                finish(
                    intent = AppLinkUtil.createBackAppCodeIntent(
                        callbackUrl = uiData.callbackUrl,
                        code = presenter.parseCode(url),
                    )
                )
            }

            override fun completeAuthWithCancellation() {
                finishWithCancellation(uiData.callbackUrl)
            }
        }
    }

    private fun finishWithCancellation(callbackUrl: String) {
        finish(
            intent = AppLinkUtil.createBackAppCancelIntent(callbackUrl)
        )
    }

    private fun finish(intent: Intent) {
        intent.setPackage(packageName)
        startActivity(intent)
        finish()
    }
}
