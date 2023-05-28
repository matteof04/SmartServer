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

package com.github.matteof04.smartserver.service.interfaces

import com.github.matteof04.smartserver.data.db.AssocState
import com.github.matteof04.smartserver.data.db.Device
import com.github.matteof04.smartserver.data.db.UpdateFrequency
import java.util.*

interface DeviceService {
    suspend fun device(id: UUID): Device?
    suspend fun allOwner(ownerId: UUID): List<Device>
    suspend fun allHouse(houseId: UUID): List<Device>
    suspend fun allHost(hostId: UUID): List<Device>
    suspend fun updateFrequency(id: UUID): UpdateFrequency?
    suspend fun changeUpdateFrequency(id: UUID, updateFrequency: UInt): Boolean
    suspend fun assocState(id: UUID): AssocState?
    suspend fun beginAssoc(id: UUID, ownerId: UUID): Boolean
    suspend fun confirmAssoc(id: UUID, hostId: UUID): Boolean
    suspend fun houseAssoc(id: UUID, houseId: UUID): Boolean
    suspend fun resetAssoc(id: UUID): Boolean
    suspend fun register(): UUID?
    suspend fun enable(id: UUID): Boolean
    suspend fun disable(id: UUID): Boolean
}