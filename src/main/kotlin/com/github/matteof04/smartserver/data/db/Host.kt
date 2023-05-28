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
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.util.*

object Hosts : Table() {
    val id = uuid("id")
    val apiKey = uuid("api_key")
    val ownerId = (uuid("owner_id") references Users.id).nullable()
    val houseId = (uuid("house_id") references Houses.id).nullable()
    val assocState = enumeration<AssociationState>("assoc_state").default(AssociationState.UNASSOCIATED)
    val enabled = bool("enabled").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Host(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) @SerialName("api_key") val apiKey: UUID,
    @Serializable(with = UUIDSerializer::class) @SerialName("owner_id") val ownerId: UUID?,
    @Serializable(with = UUIDSerializer::class) @SerialName("house_id") val houseId: UUID?,
    @SerialName("assoc_state") val assocState: AssociationState
)

data class HostPrincipal(
    val id: UUID
): Principal

@Serializable
data class HostId(
    @Serializable(with = UUIDSerializer::class) @SerialName("host_id") val id: UUID
)