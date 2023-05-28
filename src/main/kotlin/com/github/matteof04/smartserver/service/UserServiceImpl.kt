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
import com.github.matteof04.smartserver.service.interfaces.UserService
import com.github.matteof04.smartserver.util.DbUtils
import com.github.matteof04.smartserver.util.HashingUtils
import org.jetbrains.exposed.sql.*
import java.util.*

class UserServiceImpl : UserService {
    private fun resultRowToUser(row: ResultRow) = User(
        row[Users.id],
        row[Users.name],
        row[Users.mail],
        row[Users.passwordHash],
        row[Users.isAdmin]
    )

    override suspend fun user(id: UUID): User? = DbUtils.dbQuery {
        Users.select { Users.id eq id and(Users.enabled eq true) }.map(::resultRowToUser).singleOrNull()
    }

    override suspend fun userDTO(id: UUID): UserDTO? = DbUtils.dbQuery {
        Users.select { Users.id eq id and(Users.enabled eq true) }.map(::resultRowToUser).singleOrNull()?.let { UserDTO(it.id, it.name, it.mail) }
    }

    override suspend fun new(newUser: NewUser): User? = DbUtils.dbQuery {
        val insertStatement = Users.insert {
            it[Users.id] = UUID.randomUUID()
            it[Users.name] = newUser.name
            it[Users.mail] = newUser.mail
            it[Users.passwordHash] = HashingUtils.hashPassword(newUser.password)
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    override suspend fun editMail(id: UUID, mail: String): Boolean = DbUtils.dbQuery {
        Users.update({ Users.id eq id and(Users.enabled eq true) }) { it[Users.mail] = mail } > 0
    }

    override suspend fun editPassword(id: UUID, password: String): Boolean = DbUtils.dbQuery {
        Users.update({ Users.id eq id and(Users.enabled eq true) }) { it[Users.passwordHash] = HashingUtils.hashPassword(password) } > 0
    }

    override suspend fun login(userLogin: UserLogin): User? = DbUtils.dbQuery {
        Users.select { Users.mail eq userLogin.mail and(Users.passwordHash eq HashingUtils.hashPassword(userLogin.password)) and(Users.enabled eq true) }.map(::resultRowToUser).singleOrNull()
    }

    override suspend fun enable(id: UUID): Boolean = DbUtils.dbQuery {
        Users.update({ Users.id eq id and(Users.isAdmin eq false) }) {
            it[Users.enabled] = true
        } > 0
    }

    override suspend fun disable(id: UUID): Boolean = DbUtils.dbQuery {
        Users.update({ Users.id eq id and(Users.isAdmin eq false) }) {
            it[Users.enabled] = false
        } > 0
    }
}