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
        val url = request?.url.toString()
        if (!listener.isUrlForCompleteAuth(url)) {
            return false
        }

        listener.finishSuccess(url)
        return true
    }
}
