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
import com.github.matteof04.smartserver.data.db.*
import com.github.matteof04.smartserver.service.*
import com.github.matteof04.smartserver.util.DateUtils
import com.github.matteof04.smartserver.util.HashingUtils
import com.github.matteof04.smartserver.util.uuidFromParameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureRouting(){
    routing {
        val userService: UserServiceImpl by inject()
        val hostService: HostServiceImpl by inject()
        val deviceService: DeviceServiceImpl by inject()
        val houseService: HouseServiceImpl by inject()
        val thermoHygrometerDataHistoryService: ThermoHygrometerDataHistoryServiceImpl by inject()
        val jwtIssuer = application.environment.config.property("jwt.issuer").getString() //http://0.0.0.0:8080
        route("user"){
            post("new") {
                val newUser: NewUser = call.receive()
                userService.new(newUser) ?: throw InternalServerError()
                call.respond(HttpStatusCode.OK)
            }
            post("login"){
                val login: UserLogin = call.receive()
                val user = userService.login(login) ?: throw AuthenticationException()
                val jwtRefreshAudience = call.application.environment.config.property("jwt.refresh.audience").getString()
                val jwtRefreshSecret = call.application.environment.config.property("jwt.refresh.secret").getString()
                val token = JWT.create()
                    .withAudience(jwtRefreshAudience)
                    .withIssuer(jwtIssuer)
                    .withClaim("uuid", user.id.toString())
                    .withExpiresAt(DateUtils.getJWTRefreshDate())
                    .sign(Algorithm.HMAC256(jwtRefreshSecret))
                call.respond(JWTToken(token))
            }
            authenticate("jwt-refresh") {
                get("refresh") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val jwtAccessAudience = call.application.environment.config.property("jwt.access.audience").getString()
                    val jwtAccessSecret = call.application.environment.config.property("jwt.access.secret").getString()
                    val token = JWT.create()
                        .withAudience(jwtAccessAudience)
                        .withIssuer(jwtIssuer)
                        .withClaim("uuid", principal.id.toString())
                        .withExpiresAt(DateUtils.getAccessDate())
                        .sign(Algorithm.HMAC256(jwtAccessSecret))
                    call.respond(JWTToken(token))
                }
            }
            authenticate("jwt-access") {
                get("detail") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.userDTO(principal.id) ?: throw NotFoundException()
                    call.respond(user)
                }
                post("editMail") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val changeMailRequest: ChangeMailRequest = call.receive()
                    userService.editMail(principal.id, changeMailRequest.newMail)
                }
                post("editPassword") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw NotFoundException()
                    val changePasswordRequest: ChangePasswordRequest = call.receive()
                    if (HashingUtils.verifyPassword(user.passwordHash, changePasswordRequest.oldPassword)) {
                        if (userService.editPassword(principal.id, changePasswordRequest.newPassword)){
                            call.respond(HttpStatusCode.OK)
                        }else{
                            throw InternalServerError()
                        }
                    }else{
                        throw AuthorizationException()
                    }
                }
                post("enable"){
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val userId: UserId = call.receive()
                    when(userService.enable(userId.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
                post("disable"){
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val userId: UserId = call.receive()
                    when(userService.disable(userId.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
        route("device") {
            authenticate("jwt-access") {
                get("detail/{deviceId}") {
                    val deviceId = uuidFromParameter(call.parameters["deviceId"])
                    val device = deviceService.device(deviceId) ?: throw NotFoundException()
                    call.respond(device)
                }
                get("listOwner") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    call.respond(deviceService.allOwner(principal.id))
                }
                get("listHouse/{houseId}"){
                    val houseId = uuidFromParameter(call.parameters["houseId"])
                    call.respond(deviceService.allHouse(houseId))
                }
                get("listHost/{hostId}") {
                    val hostId = uuidFromParameter(call.parameters["hostId"])
                    call.respond(deviceService.allHost(hostId))
                }
                post("changeUpdateFrequency"){
                    val changeUpdateFrequency: ChangeUpdateFrequency = call.receive()
                    when(deviceService.changeUpdateFrequency(changeUpdateFrequency.deviceId, changeUpdateFrequency.updateFrequency)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.UnprocessableEntity)
                    }
                }
                post("beginAssoc") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val deviceId: DeviceId = call.receive()
                    when(deviceService.assocState(deviceId.id)?.assocState){
                        AssociationState.UNASSOCIATED -> {
                            when (deviceService.beginAssoc(deviceId.id, principal.id)){
                                true -> {
                                    call.respond(HttpStatusCode.OK)
                                    launch {
                                        delay(DateUtils.getAssocMilli())
                                        if (deviceService.assocState(deviceId.id)?.assocState != AssociationState.ASSOCIATED){
                                            deviceService.resetAssoc(deviceId.id)
                                        }
                                    }
                                }
                                false -> call.respond(HttpStatusCode.NotFound)
                            }
                        }
                        else -> call.respond(HttpStatusCode.UnprocessableEntity)
                    }
                }
                post("houseAssoc"){
                    val deviceHouseAssoc: DeviceHouseAssoc = call.receive()
                    when(deviceService.houseAssoc(deviceHouseAssoc.deviceId, deviceHouseAssoc.houseId)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
                get("register") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val deviceId = deviceService.register() ?: throw InternalServerError()
                    call.respond(DeviceId(deviceId))
                }
                post("enable"){
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val deviceId: DeviceId = call.receive()
                    when(deviceService.enable(deviceId.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
                post("disable"){
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val deviceId: DeviceId = call.receive()
                    when(deviceService.disable(deviceId.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
            authenticate("apikey") {
                get("assocState/{deviceId}") {
                    val deviceId = uuidFromParameter(call.parameters["deviceId"])
                    val assocState = deviceService.assocState(deviceId) ?: throw NotFoundException()
                    call.respond(assocState)
                }
                get("updateFrequency/{deviceId}") {
                    val deviceId = uuidFromParameter(call.parameters["deviceId"])
                    val updateFrequency = deviceService.updateFrequency(deviceId) ?: throw NotFoundException()
                    call.respond(updateFrequency)
                }
                post("confirmAssoc") {
                    val principal: HostPrincipal = call.principal() ?: throw AuthenticationException()
                    val deviceId: DeviceId = call.receive()
                    when(deviceService.assocState(deviceId.id)?.assocState){
                        AssociationState.PENDING -> {
                            when (deviceService.confirmAssoc(deviceId.id, principal.id)){
                                true -> call.respond(HttpStatusCode.OK)
                                false -> call.respond(HttpStatusCode.NotFound)
                            }
                        }
                        else -> call.respond(HttpStatusCode.UnprocessableEntity)
                    }
                }
            }
            authenticate("apikey", "jwt-access") {
                post("resetAssoc"){
                    val deviceId: DeviceId = call.receive()
                    when(call.principal<Principal>()){
                        is HostPrincipal -> {
                            val principal: HostPrincipal = call.principal() ?: throw AuthenticationException()
                            when(deviceService.device(deviceId.id)?.hostId) {
                                principal.id -> {
                                    when (deviceService.assocState(deviceId.id)?.assocState) {
                                        AssociationState.ASSOCIATED -> {
                                            when (deviceService.resetAssoc(deviceId.id)) {
                                                true -> call.respond(HttpStatusCode.OK)
                                                false -> call.respond(HttpStatusCode.NotFound)
                                            }
                                        }

                                        else -> call.respond(HttpStatusCode.UnprocessableEntity)
                                    }
                                }
                                else -> call.respond(HttpStatusCode.Unauthorized)
                            }
                        }
                        is JWTPrincipal -> {
                            val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                            when(deviceService.device(deviceId.id)?.ownerId) {
                                principal.id -> {
                                    when (deviceService.assocState(deviceId.id)?.assocState) {
                                        AssociationState.ASSOCIATED -> {
                                            when (deviceService.resetAssoc(deviceId.id)) {
                                                true -> call.respond(HttpStatusCode.OK)
                                                false -> call.respond(HttpStatusCode.NotFound)
                                            }
                                        }
                                        else -> call.respond(HttpStatusCode.UnprocessableEntity)
                                    }
                                }
                                else -> call.respond(HttpStatusCode.Unauthorized)
                            }
                        }
                        else -> {
                            call.respond(HttpStatusCode.Unauthorized)
                        }
                    }
                }
            }
        }
        route("host"){
            authenticate("jwt-access") {
                get("detail/{hostId}") {
                    val hostId = uuidFromParameter(call.parameters["hostId"])
                    val host = hostService.host(hostId) ?: throw NotFoundException()
                    call.respond(host)
                }
                get("listOwner") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    call.respond(hostService.allOwner(principal.id))
                }
                get("listHouse/{houseId}") {
                    val houseId = uuidFromParameter(call.parameters["houseId"])
                    call.respond(hostService.allHouse(houseId))
                }
                get("register") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val hostId = hostService.register() ?: throw InternalServerError()
                    call.respond(HostId(hostId))
                }
                post("beginAssoc") {
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val hostId: HostId = call.receive()
                    when(hostService.assocState(hostId.id)?.assocState){
                        AssociationState.UNASSOCIATED -> {
                            when (hostService.beginAssoc(hostId.id, principal.id)){
                                true -> {
                                    call.respond(HttpStatusCode.OK)
                                    launch {
                                        delay(DateUtils.getAssocMilli())
                                        if (hostService.assocState(hostId.id)?.assocState != AssociationState.ASSOCIATED){
                                            hostService.resetAssoc(hostId.id)
                                        }
                                    }
                                }
                                false -> call.respond(HttpStatusCode.NotFound)
                            }
                        }
                        else -> call.respond(HttpStatusCode.UnprocessableEntity)
                    }
                }
                post("houseAssoc"){
                    val hostHouseAssoc: HostHouseAssoc = call.receive()
                    when(hostService.houseAssoc(hostHouseAssoc.hostId, hostHouseAssoc.houseId)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
                post("resetAssoc"){
                    val hostId: HostId = call.receive()
                    val results = deviceService.allHost(hostId.id).map { deviceService.resetAssoc(it.id) }
                    when(hostService.resetAssoc(hostId.id) && !results.any { !it }){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.InternalServerError)
                    }
                }
                post("enable"){
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val hostId: HostId = call.receive()
                    when(hostService.enable(hostId.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
                post("disable"){
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    val user = userService.user(principal.id) ?: throw AuthenticationException()
                    if (!user.isAdmin){
                        throw AuthorizationException()
                    }
                    val hostId: HostId = call.receive()
                    when(hostService.disable(hostId.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
            authenticate("apikey") {
                get("assocState") {
                    val principal: HostPrincipal = call.principal() ?: throw AuthenticationException()
                    val assocState = hostService.assocState(principal.id) ?: throw NotFoundException()
                    call.respond(assocState)
                }
                post("confirmAssoc") {
                    val principal: HostPrincipal = call.principal() ?: throw AuthenticationException()
                    when(hostService.confirmAssoc(principal.id)){
                        true -> call.respond(HttpStatusCode.OK)
                        false -> call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
        route("house"){
            authenticate("jwt-access") {
                post("new") {
                    val newHouse: NewHouse = call.receive()
                    val principal: UserPrincipal = call.principal() ?: throw AuthenticationException()
                    houseService.new(newHouse, principal.id) ?: throw InternalServerError()
                    call.respond(HttpStatusCode.OK)
                }
                post("update") {
                    val house: House = call.receive()
                    val success = houseService.update(house)
                    if(success){
                        call.respond(HttpStatusCode.OK)
                    }else{
                        throw InternalServerError()
                    }
                }
                get("detail/{houseId}") {
                    val houseId = uuidFromParameter(call.parameters["houseId"])
                    val house = houseService.house(houseId) ?: throw NotFoundException()
                    call.respond(house)
                }
                get("list") {
                    val principal: UserPrincipal = call.principal()?: throw AuthenticationException()
                    val houses = houseService.all(principal.id)
                    call.respond(houses)
                }
                post("delete"){
                    val houseId: HouseId = call.receive()
                    if (deviceService.allHouse(houseId.id).isEmpty() && hostService.allHouse(houseId.id).isEmpty()){
                        houseService.delete(houseId.id)
                        call.respond(HttpStatusCode.OK)
                    }else{
                        call.respond(HttpStatusCode.UnprocessableEntity)
                    }
                }
            }
        }
        route("thdata"){
            authenticate("apikey") {
                post("new"){
                    val dataHistoryRecord: NewThermoHygrometerDataHistory = call.receive()
                    thermoHygrometerDataHistoryService.new(dataHistoryRecord) ?: throw InternalServerError()
                    call.respond(HttpStatusCode.OK)
                }
            }
            authenticate("jwt-access") {
                get("detail/{thDataId}") {
                    val thDataId: UUID = uuidFromParameter(call.parameters["thDataId"])
                    val record = thermoHygrometerDataHistoryService.dataHistory(thDataId) ?: throw NotFoundException()
                    call.respond(record)
                }
                get("list/{deviceId}") {
                    val deviceId = uuidFromParameter(call.parameters["deviceId"])
                    val list = thermoHygrometerDataHistoryService.all(deviceId)
                    call.respond(list)
                }
            }
        }
    }
}
