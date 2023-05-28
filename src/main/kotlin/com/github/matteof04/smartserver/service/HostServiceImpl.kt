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
import com.github.matteof04.smartserver.service.interfaces.HostService
import com.github.matteof04.smartserver.util.DbUtils
import org.jetbrains.exposed.sql.*
import java.util.*

class HostServiceImpl : HostService {
    private fun resultRowToHost(row: ResultRow): Host = Host(
        row[Hosts.id],
        row[Hosts.apiKey],
        row[Hosts.ownerId],
        row[Hosts.houseId],
        row[Hosts.assocState]
    )

    override suspend fun host(id: UUID): Host? = DbUtils.dbQuery {
        Hosts.select { Hosts.id eq id and(Hosts.enabled eq true) }.map(::resultRowToHost).singleOrNull()
    }

    override suspend fun allOwner(ownerId: UUID): List<Host> = DbUtils.dbQuery {
        Hosts.select { Hosts.ownerId eq ownerId and(Hosts.enabled eq true) }.map(::resultRowToHost)
    }

    override suspend fun allHouse(houseId: UUID): List<Host> = DbUtils.dbQuery {
        Hosts.select { Hosts.ownerId eq houseId and(Hosts.enabled eq true) }.map(::resultRowToHost)
    }

    override suspend fun assocState(id: UUID): AssocState? = DbUtils.dbQuery {
        host(id)?.assocState?.let { AssocState(it) }
    }

    override suspend fun beginAssoc(id: UUID, ownerId: UUID): Boolean = DbUtils.dbQuery {
        Hosts.update({ Hosts.id eq id and(Hosts.enabled eq true) }){
            it[Hosts.assocState] = AssociationState.PENDING
            it[Hosts.ownerId] = ownerId
        } > 0
    }

    override suspend fun confirmAssoc(id: UUID): Boolean = DbUtils.dbQuery {
        Hosts.update({ Hosts.id eq id and(Hosts.enabled eq true) }){
            it[Hosts.assocState] = AssociationState.ASSOCIATED
        } > 0
    }

    override suspend fun houseAssoc(id: UUID, houseId: UUID): Boolean = DbUtils.dbQuery {
        Hosts.update({ Hosts.id eq id and(Hosts.enabled eq true) }){
            it[Hosts.houseId] = houseId
        } > 0
    }

    override suspend fun resetAssoc(id: UUID): Boolean = DbUtils.dbQuery {
        Hosts.update({ Hosts.id eq id and(Hosts.enabled eq true) }){
            it[Hosts.assocState] = AssociationState.UNASSOCIATED
            it[Hosts.ownerId] = null
            it[Hosts.houseId] = null
        } > 0
    }

    override suspend fun login(apiKey: UUID): HostPrincipal? = DbUtils.dbQuery {
        Hosts.select { Hosts.apiKey eq apiKey and(Hosts.enabled eq true) }.map(::resultRowToHost).singleOrNull()?.let { HostPrincipal(it.id) }
    }

    override suspend fun register(): UUID? = DbUtils.dbQuery {
        val insertStatement = Hosts.insert {
            it[Hosts.id] = UUID.randomUUID()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToHost)?.id
    }

    override suspend fun enable(id: UUID): Boolean = DbUtils.dbQuery {
        Hosts.update({Hosts.id eq id}) {
            it[Hosts.enabled] = true
        } > 0
    }

    override suspend fun disable(id: UUID): Boolean = DbUtils.dbQuery {
        Hosts.update({Hosts.id eq id}) {
            it[Hosts.enabled] = false
        } > 0
    }
}