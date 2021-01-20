package ru.tinkoff.core.tinkoffId

/**
 * @author Stanislav Mukhametshin
 *
 * Model that contains tinkoff session information
 */
public data class TinkoffTokenPayload(
    // token to access Tinkoff Api
    val accessToken: String,
    // time after which access token will expire
    val expiresIn: Int,
    // user id in jwt format
    val idToken: String?,
    // token needed to get new accessToken
    val refreshToken: String
)