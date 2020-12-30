package ru.tinkoff.core.tinkoffId

import ru.tinkoff.core.tinkoffId.error.TinkoffRequestException

/**
 * @author Stanislav Mukhametshin
 *
 * Main class to perform requests from Tinkoff Api
 */
public interface TinkoffCall<T> {

    /**
     * Function for synchronous operations
     * NOTE should not be called from the main thread
     *
     * @return T
     *
     * @throws TinkoffRequestException if something goes wrong.
     * It can contain message {@link TinkoffErrorMessage} with problem description
     */
    @Throws(TinkoffRequestException::class)
    public fun getResponse(): T

    /**
     * Function to cancel TinkoffCall
     */
    public fun cancel()
}