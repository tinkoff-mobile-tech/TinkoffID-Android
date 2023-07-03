package ru.tinkoff.core.tinkoffId.ui.webView

/**
 * @author k.voskrebentsev
 */
internal interface TinkoffWebViewListener {

    fun isUrlForCompleteAuth(url: String): Boolean

    fun finishCancellation()

    fun finishSuccess(code: String)
}
