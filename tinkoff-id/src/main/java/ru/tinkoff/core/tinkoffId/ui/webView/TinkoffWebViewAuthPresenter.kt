package ru.tinkoff.core.tinkoffId.ui.webView

import ru.tinkoff.core.tinkoffId.api.TinkoffIdApi

/**
 * @author k.voskrebentsev
 */
internal class TinkoffWebViewAuthPresenter {

    fun buildWebViewAuthStartUrl(uiData: TinkoffWebViewUiData): String {
        return TinkoffIdApi.buildWebViewAuthStartUrl(
            clientId = uiData.clientId,
            codeChallenge = uiData.codeChallenge,
            codeChallengeMethod = uiData.codeChallengeMethod,
            redirectUri = uiData.redirectUri,
        )
    }

    fun parseCode(url: String): String {
        return TinkoffIdApi.parseCode(url)
    }
}
