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

import com.github.matteof04.smartserver.data.db.*
import com.github.matteof04.smartserver.service.interfaces.DeviceService
import com.github.matteof04.smartserver.util.DbUtils
import org.jetbrains.exposed.sql.*
import java.util.*

class DeviceServiceImpl : DeviceService {
    private fun resultRowToDevice(row: ResultRow): Device = Device(
        row[Devices.id],
        row[Devices.type],
        row[Devices.ownerId],
        row[Devices.houseId],
        row[Devices.hostId],
        row[Devices.updateFrequency],
        row[Devices.assocState]
    )

    override suspend fun device(id: UUID): Device? = DbUtils.dbQuery {
        Devices.select { Devices.id eq id and(Devices.enabled eq true) }.map(::resultRowToDevice).singleOrNull()
    }

    override suspend fun allOwner(ownerId: UUID): List<Device> = DbUtils.dbQuery {
        Devices.select { Devices.ownerId eq ownerId and(Devices.enabled eq true) }.map(::resultRowToDevice)
    }

    override suspend fun allHouse(houseId: UUID): List<Device> = DbUtils.dbQuery {
        Devices.select { Devices.houseId eq houseId and(Devices.enabled eq true) }.map(::resultRowToDevice)
    }

    override suspend fun allHost(hostId: UUID): List<Device> = DbUtils.dbQuery {
        Devices.select { Devices.hostId eq hostId and(Devices.enabled eq true) }.map(::resultRowToDevice)
    }

    override suspend fun changeUpdateFrequency(id: UUID, updateFrequency: UInt): Boolean = DbUtils.dbQuery {
        Devices.update({ Devices.id eq id and(Devices.enabled eq true) }){
            it[Devices.updateFrequency] = updateFrequency
        } > 0
    }

    override suspend fun updateFrequency(id: UUID): UpdateFrequency? = DbUtils.dbQuery {
        device(id)?.updateFrequency?.let { UpdateFrequency(it) }
    }

    override suspend fun assocState(id: UUID): AssocState? = DbUtils.dbQuery {
        device(id)?.assocState?.let { AssocState(it) }
    }

    override suspend fun beginAssoc(id: UUID, ownerId: UUID): Boolean = DbUtils.dbQuery {
        Devices.update({ Devices.id eq id and(Devices.enabled eq true) }){
            it[Devices.assocState] = AssociationState.PENDING
            it[Devices.ownerId] = ownerId
        } > 0
    }

    override suspend fun confirmAssoc(id: UUID, hostId: UUID): Boolean = DbUtils.dbQuery {
        Devices.update({ Devices.id eq id and(Devices.enabled eq true) }){
            it[Devices.assocState] = AssociationState.ASSOCIATED
            it[Devices.hostId] = hostId
        } > 0
    }

    override suspend fun houseAssoc(id: UUID, houseId: UUID): Boolean = DbUtils.dbQuery {
        Devices.update({ Devices.id eq id and(Devices.enabled eq true) }){
            it[Devices.houseId] = houseId
        } > 0
    }

    override suspend fun resetAssoc(id: UUID): Boolean = DbUtils.dbQuery {
        Devices.update({ Devices.id eq id and(Devices.enabled eq true) }){
            it[Devices.assocState] = AssociationState.UNASSOCIATED
            it[Devices.hostId] = null
            it[Devices.ownerId] = null
            it[Devices.houseId] = null
        } > 0
    }

    override suspend fun register(): UUID? = DbUtils.dbQuery {
        val insertStatement = Devices.insert {
            it[Devices.id] = UUID.randomUUID()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToDevice)?.id
    }

    override suspend fun enable(id: UUID): Boolean = DbUtils.dbQuery {
        Devices.update({Devices.id eq id}) {
            it[Devices.enabled] = true
        } > 0
    }

    override suspend fun disable(id: UUID): Boolean = DbUtils.dbQuery {
        Devices.update({Devices.id eq id}) {
            it[Devices.enabled] = false
        } > 0
    }
}