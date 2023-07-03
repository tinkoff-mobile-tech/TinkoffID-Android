package ru.tinkoff.core.tinkoffId.ui.webView

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.core.tinkoffId.AppLinkUtil
import ru.tinkoff.core.tinkoffId.R

/**
 * @author k.voskrebentsev
 */
internal class TinkoffWebViewAuthActivity : AppCompatActivity() {

    private val presenter: TinkoffWebViewAuthPresenter by lazy { TinkoffWebViewAuthPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tinkoff_id_web_view_activity)
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val webView = findViewById<WebView>(R.id.webView)
        val uiData = AppLinkUtil.parseTinkoffWebViewUiData(intent)
        val url = presenter.getStartWebViewAuthUrl(uiData)
        with(webView) {
            webViewClient = TinkoffWebViewClient(createTinkoffWebViewCallback(uiData))
            loadUrl(url)
            settings.javaScriptEnabled = true
        }
    }

    private fun createTinkoffWebViewCallback(uiData: TinkoffWebViewUiData): TinkoffWebViewListener {
        return object : TinkoffWebViewListener {

            override fun isUrlForCompleteAuth(url: String): Boolean {
                return url.startsWith(uiData.redirectUri)
            }

            override fun finishCancellation() {
                finish(
                    intent = AppLinkUtil.createBackAppCancelIntent(uiData.callbackUrl)
                )
            }

            override fun finishSuccess(code: String) {
                finish(
                    intent = AppLinkUtil.createBackAppCodeIntent(
                        callbackUrl = uiData.callbackUrl,
                        code = code,
                    )
                )
            }

            private fun finish(intent: Intent) {
                startActivity(intent)
                finish()
            }
        }
    }
}
