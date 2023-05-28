/*
 * Copyright (C) 2023 Matteo Franceschini <matteof5730@gmail.com>
 *
 * This file is part of SmartServer.
 * SmartServer is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * SmartServer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with SmartServer.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.matteof04.smartserver.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.matteof04.smartserver.data.db.UserPrincipal
import com.github.matteof04.smartserver.service.HostServiceImpl
import com.github.matteof04.smartserver.service.UserServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureSecurity(){
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtRefreshAudience = environment.config.property("jwt.refresh.audience").getString()
    val jwtRefreshSecret = environment.config.property("jwt.refresh.secret").getString()
    val jwtRefreshRealm = environment.config.property("jwt.refresh.realm").getString()
    val jwtAccessAudience = environment.config.property("jwt.access.audience").getString()
    val jwtAccessSecret = environment.config.property("jwt.access.secret").getString()
    val jwtAccessRealm = environment.config.property("jwt.access.realm").getString()
    val apiKeyRealm = environment.config.property("apiKey.realm").getString()
    install(StatusPages) {
        exception<AuthenticationException> { call, _ ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<AuthorizationException> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<ContentTransformationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<InternalServerError> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError)
        }
        exception<NotFoundException> { call, _ ->
            call.respond(HttpStatusCode.NotFound)
        }
    }
    install(Authentication){
        bearer("apikey"){
            realm = apiKeyRealm
            authenticate { bearerTokenCredential ->
                val hostService: HostServiceImpl by inject()
                val apiKey = kotlin.runCatching { UUID.fromString(bearerTokenCredential.token) }.getOrNull()
                apiKey?.let { hostService.login(it) }
            }
        }
        jwt("jwt-refresh") {
            realm = jwtRefreshRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtRefreshSecret))
                    .withAudience(jwtRefreshAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val userService: UserServiceImpl by inject()
                val userId = kotlin.runCatching { UUID.fromString(credential.payload.getClaim("uuid").asString()) }.getOrNull()
                userId?.let { userService.user(it) }?.let { UserPrincipal(it.id) }
            }
            challenge { _, _ ->  call.respond(HttpStatusCode.Unauthorized)}
        }
        jwt("jwt-access") {
            realm = jwtAccessRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtAccessSecret))
                    .withAudience(jwtAccessAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val userService: UserServiceImpl by inject()
                val userId = kotlin.runCatching { UUID.fromString(credential.payload.getClaim("uuid").asString()) }.getOrNull()
                userId?.let { userService.user(it) }?.let { UserPrincipal(it.id) }
            }
            challenge { _, _ ->  call.respond(HttpStatusCode.Unauthorized)}
        }
    }
}

class AuthenticationException : Exception()
class AuthorizationException : Exception()
class InternalServerError: Exception()

@Serializable
data class JWTToken(
    val token: String
)