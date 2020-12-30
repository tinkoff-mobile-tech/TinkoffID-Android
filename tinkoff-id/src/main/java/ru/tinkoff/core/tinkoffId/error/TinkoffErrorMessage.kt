package ru.tinkoff.core.tinkoffId.error

/**
 * @author Stanislav Mukhametshin
 *
 * Information about errors received in request responses
 */
public class TinkoffErrorMessage(
    // human readable message of api error
    public val message: String?,
    // error type returned after sending request to endpoints, you can find all error types
    // in TinkoffTokenErrorConstants, TokenSignOutErrorConstants
    public val errorType: Int
)