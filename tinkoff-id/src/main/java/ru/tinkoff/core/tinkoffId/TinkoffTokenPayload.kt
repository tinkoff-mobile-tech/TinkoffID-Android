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