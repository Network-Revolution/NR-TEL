/*
 * Copyright 2020- Network Revolution Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.dokoden.nr_tel.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {
    @Insert
    fun insert(accountData: AccountData)

    @Update
    fun update(accountData: AccountData)

    @Delete
    fun delete(accountData: AccountData)

    @Query("DELETE FROM AccountData")
    fun deleteAll()

    @Query("SELECT * FROM AccountData ORDER BY orderNumber ASC")
    fun getAll(): LiveData<List<AccountData>>

    //automatically stores 1 for true and 0 for false
    @Query("SELECT * FROM AccountData WHERE accountActive = 1 ORDER BY orderNumber ASC")
    fun getEnabled(): LiveData<List<AccountData>>

    @Query("SELECT * FROM AccountData WHERE accountOutgoing = 1 AND accountActive = 1 ORDER BY orderNumber ASC")
    fun getOutgoing(): LiveData<List<AccountData>>

    @Query("SELECT * FROM AccountData WHERE orderNumber = :id")
    fun getAccountById(id: Int): AccountData

    @Query("UPDATE AccountData SET accountOutgoing = 0")
    fun clearOutgoing()

    @Query("UPDATE AccountData SET accountOutgoing = 1 WHERE orderNumber = :id")
    fun setOutgoing(id: Int)

    @Query("UPDATE AccountData SET orderNumber = (SELECT (count(*)+1)*2 FROM AccountData AS b WHERE AccountData.orderNumber > b.orderNumber)")
    fun shortViewModel()
}
