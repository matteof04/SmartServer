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

package com.github.matteof04.smartserver.data.db

import com.github.matteof04.smartserver.data.serializer.UUIDSerializer
import com.github.matteof04.smartserver.util.DbUtils
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.util.*

object Users : Table() {
    val id = uuid("id")
    val name = varchar("name", DbUtils.varcharDefaultLength)
    val mail = varchar("mail", DbUtils.varcharDefaultLength)
    val passwordHash = varchar("password_hash", DbUtils.varcharDefaultLength)
    val isAdmin = bool("admin").default(false)
    val enabled = bool("enabled").default(true)

    override val primaryKey = PrimaryKey(id)
}

data class User(
    val id: UUID,
    val name: String,
    val mail: String,
    val passwordHash: String,
    val isAdmin: Boolean
)

@Serializable
data class NewUser(
    val name: String,
    val mail: String,
    val password: String
)

@Serializable
data class UserDTO(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val mail: String
)

@Serializable
data class UserLogin(
    val mail: String,
    val password: String
)

@Serializable
data class ChangeMailRequest(
    @SerialName("new_mail") val newMail: String
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("old_password") val oldPassword: String,
    @SerialName("new_password") val newPassword: String
)

data class UserPrincipal(
    val id: UUID
): Principal

@Serializable
data class UserId(
    @Serializable(with = UUIDSerializer::class) @SerialName("user_id") val id: UUID
)