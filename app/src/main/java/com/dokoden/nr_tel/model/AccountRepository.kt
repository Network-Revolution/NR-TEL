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

import androidx.annotation.WorkerThread

class AccountRepository(private val accountDao: AccountDao) {
    val allAccount = accountDao.getAll()
    val enabledAccount = accountDao.getEnabled()
    val outgoingAccount = accountDao.getOutgoing()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(accountData: AccountData) {
        accountDao.insert(accountData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(accountData: AccountData) {
        accountDao.update(accountData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(accountData: AccountData) {
        accountDao.delete(accountData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAccountById(id: Int) = accountDao.getAccountById(id)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun clearOutgoing() = accountDao.clearOutgoing()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun setOutgoing(id: Int) = accountDao.setOutgoing(id)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun shortViewModel() {
        accountDao.shortViewModel()
    }
}
