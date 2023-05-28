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

package com.github.matteof04.smartserver

import com.github.matteof04.smartserver.plugins.*
import io.ktor.server.application.*


fun main(args: Array<String>): Unit =
    io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    log.info("""
        SmartServer  Copyright (C) 2023  Matteo Franceschini
        This program comes with ABSOLUTELY NO WARRANTY.
        This is free software, and you are welcome to redistribute it
        under certain conditions.
    """.trimIndent())
    configureDatabase()
    configureKoin()
    configureHTTP()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
