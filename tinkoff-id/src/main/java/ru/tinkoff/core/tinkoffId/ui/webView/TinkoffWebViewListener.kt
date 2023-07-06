package ru.tinkoff.core.tinkoffId.ui.webView

/**
 * @author k.voskrebentsev
 */
internal interface TinkoffWebViewListener {

    fun isUrlForCompleteAuth(url: String): Boolean

    fun finishSuccess(url: String)
}
