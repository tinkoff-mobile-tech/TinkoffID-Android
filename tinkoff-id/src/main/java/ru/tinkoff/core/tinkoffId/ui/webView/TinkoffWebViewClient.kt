package ru.tinkoff.core.tinkoffId.ui.webView

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * @author k.voskrebentsev
 */
internal class TinkoffWebViewClient(
    private val listener: TinkoffWebViewListener,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return processUrl(request?.url.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return processUrl(url)
    }

    private fun processUrl(url: String?): Boolean {
        if (url == null || !listener.isUrlForCompleteAuth(url)) {
            return false
        }

        listener.finishSuccess(url)
        return true
    }
}
