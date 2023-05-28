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

import com.github.matteof04.smartserver.data.db.House
import com.github.matteof04.smartserver.data.db.NewHouse
import java.util.*

interface HouseService {
    suspend fun house(id: UUID): House?
    suspend fun new(newHouse: NewHouse, ownerId: UUID): House?
    suspend fun update(house: House): Boolean
    suspend fun all(ownerId: UUID): List<House>
    suspend fun delete(houseId: UUID): Boolean
}