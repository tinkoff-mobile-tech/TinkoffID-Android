package ru.tinkoff.core.tinkoffId.error

/**
 * Endpoint /auth/revoke
 * Error types when revoking token
 */
public object TokenSignOutErrorConstants {

    /**
     * User is not contains in audience
     */
    public const val INVALID_GRANT: Int = 1

    /**
     * There is no token in request
     */
    public const val INVALID_REQUEST: Int = 2

    /**
     * Unknown type of error
     */
    public const val UNKNOWN_ERROR: Int = 3
}