package ru.tinkoff.core.tinkoffId.ui.webView

import android.net.Uri
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

        finishAuth(url)
        return true
    }

    private fun finishAuth(url: String) {
        val code = requireNotNull(
            Uri.parse(url).getQueryParameter(FIELD_CODE)
        ) {
            "The server must specify the code when completing authorization"
        }

        listener.finishSuccess(code)
    }

    private companion object {
        const val FIELD_CODE = "code"
    }
}
