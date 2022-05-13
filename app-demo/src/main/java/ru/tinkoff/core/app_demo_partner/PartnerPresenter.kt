package ru.tinkoff.core.app_demo_partner

import android.net.Uri
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.tinkoff.core.tinkoffId.TinkoffCall
import ru.tinkoff.core.tinkoffId.TinkoffIdAuth
import ru.tinkoff.core.tinkoffId.TinkoffIdStatusCode
import ru.tinkoff.core.tinkoffId.TinkoffTokenPayload

/**
 * @author Stanislav Mukhametshin
 */
class PartnerPresenter(
    private val tinkoffPartnerAuth: TinkoffIdAuth,
    private val partnerActivity: PartnerActivity
) : DefaultLifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private var tokenPayload: TinkoffTokenPayload? = null

    init {
        partnerActivity.lifecycle.addObserver(this)
    }

    fun getToken(uri: Uri) {
        when (tinkoffPartnerAuth.getStatusCode(uri)) {
            TinkoffIdStatusCode.SUCCESS -> {
                tinkoffPartnerAuth.getTinkoffTokenPayload(uri)
                    .toSingle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Log.d(LOG_TAG, it.accessToken)
                            tokenPayload = it
                            partnerActivity.onTokenAvailable()
                        },
                        {
                            Log.e(LOG_TAG, "GetToken Error", it)
                            partnerActivity.onAuthError()
                        }
                    ).apply {
                        compositeDisposable.add(this)
                    }
            }
            TinkoffIdStatusCode.CANCELLED_BY_USER -> {
                partnerActivity.onCancelledByUser()
            }
            else -> {
                partnerActivity.onAuthError()
            }
        }
    }

    fun refreshToken() {
        val refreshToken = tokenPayload?.refreshToken ?: return
        tinkoffPartnerAuth.obtainTokenPayload(refreshToken)
            .toSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d(LOG_TAG, it.accessToken)
                    partnerActivity.onTokenRefresh()
                    tokenPayload = it
                },
                {
                    Log.e(LOG_TAG, "RefreshToken Error", it)
                }
            ).apply {
                compositeDisposable.add(this)
            }
    }

    fun revokeToken() {
        val refreshToken = tokenPayload?.refreshToken ?: return
        tinkoffPartnerAuth.signOutByRefreshToken(refreshToken)
            .toSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d(LOG_TAG, "Token Revoked")
                    partnerActivity.onTokenRevoke()
                },
                {
                    Log.e(LOG_TAG, "RevokeToken Error", it)
                }
            ).apply {
                compositeDisposable.add(this)
            }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        compositeDisposable.clear()
    }

    private fun <T> TinkoffCall<T>.toSingle(): Single<T> {
        return Single.fromCallable { getResponse() }
            .doOnDispose { cancel() }
    }

    private companion object {

        private const val LOG_TAG = "TokenResponse"
    }
}
