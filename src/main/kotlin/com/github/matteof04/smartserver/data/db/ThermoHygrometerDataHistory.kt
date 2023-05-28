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

import com.github.matteof04.smartserver.data.serializer.InstantSerializer
import com.github.matteof04.smartserver.data.serializer.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object ThermoHygrometerDataHistories : Table() {
    val id = uuid("id")
    val deviceId = uuid("device_id") references Devices.id
    val timestamp = timestamp("timestamp")
    val batteryPercentage = uinteger("battery_percentage")
    val temperature = float("temperature")
    val humidity = float("humidity")
    val heatIndex = float("heat_index")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ThermoHygrometerDataHistory(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) @SerialName("device_id") val deviceId: UUID,
    @Serializable(with = InstantSerializer::class) val timestamp: Instant,
    @SerialName("battery_percentage") val batteryPercentage: UInt,
    val temperature: Float,
    val humidity: Float,
    @SerialName("heat_index") val heatIndex: Float
)

@Serializable
data class NewThermoHygrometerDataHistory(
    @Serializable(with = UUIDSerializer::class) @SerialName("device_id") val deviceId: UUID,
    @SerialName("battery_percentage") val batteryPercentage: UInt,
    val temperature: Float,
    val humidity: Float,
    @SerialName("heat_index") val heatIndex: Float
)
