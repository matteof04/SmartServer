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

package com.github.matteof04.smartserver.plugins

import com.github.matteof04.smartserver.data.db.*
import com.github.matteof04.smartserver.util.DbUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils

fun Application.configureDatabase(){
    val dbConfig = HikariConfig(environment.config.property("HIKARI_CONFIG").getString())
    val dataSource = HikariDataSource(dbConfig)
    Database.connect(dataSource)
    runBlocking {
        createTables()
    }
}

suspend fun createTables() = DbUtils.dbQuery {
    SchemaUtils.create(Users)
    SchemaUtils.create(Houses)
    SchemaUtils.create(Hosts)
    SchemaUtils.create(Devices)
    SchemaUtils.create(ThermoHygrometerDataHistories)
}