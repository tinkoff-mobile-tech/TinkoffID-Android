package ru.tinkoff.core.tinkoffId.error

/**
 * @author Stanislav Mukhametshin
 *
 * Any exception related to requests is wrapped to TinkoffRequestException
 */
public class TinkoffRequestException(
    public val reason: Throwable,
    override val message: String? = null,
    // information about Tinkoff Api errors
    public val errorMessage: TinkoffErrorMessage? = null
) : Exception(message, reason)