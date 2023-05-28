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

import com.github.matteof04.smartserver.data.db.House
import com.github.matteof04.smartserver.data.db.Houses
import com.github.matteof04.smartserver.data.db.NewHouse
import com.github.matteof04.smartserver.service.interfaces.HouseService
import com.github.matteof04.smartserver.util.DbUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class HouseServiceImpl : HouseService {
    private fun resultRowToHouse(row: ResultRow): House = House(
        row[Houses.id],
        row[Houses.name],
        row[Houses.ownerId]
    )

    override suspend fun house(id: UUID): House? = DbUtils.dbQuery {
        Houses.select { Houses.id eq id }.map(::resultRowToHouse).singleOrNull()
    }

    override suspend fun new(newHouse: NewHouse, ownerId: UUID): House? = DbUtils.dbQuery {
        val insertStatement = Houses.insert {
            it[Houses.id] = UUID.randomUUID()
            it[Houses.name] = newHouse.name
            it[Houses.ownerId] = ownerId
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToHouse)
    }

    override suspend fun update(house: House): Boolean = DbUtils.dbQuery {
        Houses.update({ Houses.id eq house.id }) {
            it[Houses.name] = house.name
        } > 0
    }

    override suspend fun all(ownerId: UUID): List<House> = DbUtils.dbQuery {
        Houses.select { Houses.ownerId eq ownerId }.map(::resultRowToHouse)
    }

    override suspend fun delete(houseId: UUID): Boolean = DbUtils.dbQuery {
        Houses.deleteWhere(0, 0) { Houses.id eq houseId } > 0
    }
}