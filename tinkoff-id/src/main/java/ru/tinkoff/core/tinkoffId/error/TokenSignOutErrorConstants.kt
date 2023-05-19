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
 * Endpoint /auth/revoke.
 * Error types when revoking token
 *
 * @author Stanislav Mukhametshin
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
