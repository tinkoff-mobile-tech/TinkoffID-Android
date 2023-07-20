package ru.tinkoff.core.tinkoffId.ui.webView

/**
 * @author k.voskrebentsev
 */
internal interface TinkoffWebViewListener {

    fun isUrlForAuthCompletion(url: String): Boolean

    fun completeAuthWithSuccess(url: String)

    fun completeAuthWithCancellation()
}
