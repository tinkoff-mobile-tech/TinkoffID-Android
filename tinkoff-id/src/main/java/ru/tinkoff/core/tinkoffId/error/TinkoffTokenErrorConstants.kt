/*
 * Copyright © 2021 Tinkoff Bank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.core.tinkoffId.error

/**
 * Endpoint /auth/token.
 * Error types when refreshing or obtaining token
 *
 * @author Stanislav Mukhametshin
 */
public object TinkoffTokenErrorConstants {

    /**
     * There is not required parameters, cookies, headers and so on
     */
    public const val INVALID_REQUEST: Int = 1

    /**
     * redirect_uri does not match the client
     */
    public const val INVALID_CLIENT: Int = 2

    /**
     * Invalid/expired refresh_token or code passed
     */
    public const val INVALID_GRANT: Int = 3

    /**
     * No required headers
     */
    public const val UNAUTHORIZED_CLIENT: Int = 4

    /**
     * Unknown grant_type passed
     */
    public const val UNSUPPORTED_GRANT_TYPE: Int = 5

    /**
     *  Something went wrong. You can try to repeat request again
     */
    public const val SERVER_ERROR: Int = 6

    /**
     *  The app is asking for tokens too often (current limit is 50 per hour), it might be worth looking for a bug in the app
     */
    public const val LIMIT_EXCEEDED: Int = 7

    /**
     *  Unknown error type returned
     */
    public const val UNKNOWN_ERROR: Int = 8
}
