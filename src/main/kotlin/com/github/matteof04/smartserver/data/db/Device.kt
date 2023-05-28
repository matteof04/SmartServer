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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.util.*

enum class DeviceTypes {
    TH_SENSOR,
    PLANT_SENSOR
}

enum class AssociationState {
    ASSOCIATED,
    PENDING,
    UNASSOCIATED
}

object Devices : Table() {
    val id = uuid("id")
    val type = enumeration<DeviceTypes>("type")
    val ownerId = (uuid("owner_id") references Users.id).nullable()
    val houseId = (uuid("house_id") references Houses.id).nullable()
    val hostId = (uuid("host_id") references Hosts.id).nullable()
    val updateFrequency = uinteger("update_frequency").default(0u)
    val assocState = enumeration<AssociationState>("assoc_state").default(AssociationState.UNASSOCIATED)
    val enabled = bool("enabled").default(true)

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Device(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val type: DeviceTypes,
    @Serializable(with = UUIDSerializer::class) @SerialName("owner_id") val ownerId: UUID?,
    @Serializable(with = UUIDSerializer::class) @SerialName("house_id") val houseId: UUID?,
    @Serializable(with = UUIDSerializer::class) @SerialName("host_id") val hostId: UUID?,
    @SerialName("update_frequency") val updateFrequency: UInt,
    @SerialName("assoc_state") val assocState: AssociationState
)

@Serializable
data class UpdateFrequency(
    @SerialName("update_frequency") val updateFrequency: UInt
)

@Serializable
data class ChangeUpdateFrequency(
    @Serializable(with = UUIDSerializer::class) @SerialName("device_id") val deviceId: UUID,
    @SerialName("update_frequency") val updateFrequency: UInt
)

@Serializable
data class AssocState(
    @SerialName("assoc_state") val assocState: AssociationState
)

@Serializable
data class DeviceId(
    @Serializable(with = UUIDSerializer::class) @SerialName("device_id") val id: UUID
)
