package ru.tinkoff.core.tinkoffId.ui.webView

import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * @author k.voskrebentsev
 */
internal class TinkoffWebViewClient(
    private val listener: TinkoffWebViewListener,
) : WebViewClient() {

    private var lastUrl: String? = null

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return processUrl(request?.url.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return processUrl(url)
    }

    private fun processUrl(url: String?): Boolean {
        if (url == null || !listener.isUrlForAuthCompletion(url)) {
            lastUrl = url
            return false
        }

        lastUrl?.let { clearCookies(it) }
        listener.completeAuthWithSuccess(url)
        return true
    }

    private fun clearCookies(url: String) {
        val cookieManager = CookieManager.getInstance()
        val cookiesNames = CookieManager.getInstance().getCookie(url)
            .split(COOKIES_SEPARATOR)
            .map { cookie ->
                val endIndexOfCookieName = cookie.indexOf(COOKIE_NAME_AND_VALUE_SEPARATOR)
                cookie.substring(0, endIndexOfCookieName)
            }
        cookiesNames.forEach { cookieName ->
            cookieManager.setCookie(url, "$cookieName$COOKIE_NAME_AND_VALUE_SEPARATOR")
        }
        cookieManager.flush()
    }

    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        listener.completeAuthWithCancellation()
        return true
    }

    private companion object {
        const val COOKIES_SEPARATOR = "; "
        const val COOKIE_NAME_AND_VALUE_SEPARATOR = "="
    }
}
