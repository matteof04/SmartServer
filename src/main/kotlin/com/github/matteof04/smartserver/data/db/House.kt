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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.util.*

object Houses : Table() {
    val id = uuid("id")
    val name = varchar("name", DbUtils.varcharDefaultLength)
    val ownerId = uuid("owner_id") references Users.id

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class House(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    @Serializable(with = UUIDSerializer::class) @SerialName("owner_id") val ownerId: UUID
)

@Serializable
data class NewHouse(
    val name: String
)

@Serializable
data class DeviceHouseAssoc(
    @Serializable(with = UUIDSerializer::class) @SerialName("device_id") val deviceId: UUID,
    @Serializable(with = UUIDSerializer::class) @SerialName("house_id") val houseId: UUID
)

@Serializable
data class HostHouseAssoc(
    @Serializable(with = UUIDSerializer::class) @SerialName("host_id") val hostId: UUID,
    @Serializable(with = UUIDSerializer::class) @SerialName("house_id") val houseId: UUID
)

@Serializable
data class HouseId(
    @Serializable(with = UUIDSerializer::class) @SerialName("house_id") val id: UUID
)