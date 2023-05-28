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

package com.github.matteof04.smartserver.service

import com.github.matteof04.smartserver.data.db.NewThermoHygrometerDataHistory
import com.github.matteof04.smartserver.data.db.ThermoHygrometerDataHistories
import com.github.matteof04.smartserver.data.db.ThermoHygrometerDataHistory
import com.github.matteof04.smartserver.service.interfaces.ThermoHygrometerDataHistoryService
import com.github.matteof04.smartserver.util.DbUtils
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.*

class ThermoHygrometerDataHistoryServiceImpl : ThermoHygrometerDataHistoryService {
    private fun resultRowToDataHistory(row: ResultRow): ThermoHygrometerDataHistory = ThermoHygrometerDataHistory(
        row[ThermoHygrometerDataHistories.id],
        row[ThermoHygrometerDataHistories.deviceId],
        row[ThermoHygrometerDataHistories.timestamp],
        row[ThermoHygrometerDataHistories.batteryPercentage],
        row[ThermoHygrometerDataHistories.temperature],
        row[ThermoHygrometerDataHistories.humidity],
        row[ThermoHygrometerDataHistories.heatIndex]
    )

    override suspend fun dataHistory(id: UUID): ThermoHygrometerDataHistory? = DbUtils.dbQuery {
        ThermoHygrometerDataHistories.select { ThermoHygrometerDataHistories.id eq id }.map(::resultRowToDataHistory).singleOrNull()
    }

    override suspend fun new(newThermoHygrometerDataHistory: NewThermoHygrometerDataHistory): ThermoHygrometerDataHistory? = DbUtils.dbQuery {
        val insertStatement = ThermoHygrometerDataHistories.insert {
            it[ThermoHygrometerDataHistories.id] = UUID.randomUUID()
            it[ThermoHygrometerDataHistories.deviceId] = newThermoHygrometerDataHistory.deviceId
            it[ThermoHygrometerDataHistories.timestamp] = Instant.now()
            it[ThermoHygrometerDataHistories.batteryPercentage] = newThermoHygrometerDataHistory.batteryPercentage
            it[ThermoHygrometerDataHistories.temperature] = newThermoHygrometerDataHistory.temperature
            it[ThermoHygrometerDataHistories.humidity] = newThermoHygrometerDataHistory.humidity
            it[ThermoHygrometerDataHistories.heatIndex] = newThermoHygrometerDataHistory.heatIndex
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToDataHistory)
    }

    override suspend fun all(deviceId: UUID): List<ThermoHygrometerDataHistory> = DbUtils.dbQuery {
        ThermoHygrometerDataHistories.select { ThermoHygrometerDataHistories.deviceId eq deviceId }.map(::resultRowToDataHistory)
    }
}