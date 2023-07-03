package ru.tinkoff.core.tinkoffId.ui.webView

/**
 * @author k.voskrebentsev
 */
internal class TinkoffWebViewUiData(
    val clientId: String,
    val codeChallenge: String,
    val codeChallengeMethod: String,
    val redirectUri: String,
    val callbackUrl: String,
)
