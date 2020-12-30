package ru.tinkoff.core.tinkoffId

/**
 * Status when returning to the partner application after requesting partner authorization
 */
public enum class TinkoffIdStatusCode {

    /** Success: authorization succeeded, you can retrieve the code for requesting a token*/
    SUCCESS,

    /** Authorization has been canceled by the user */
    CANCELLED_BY_USER
}