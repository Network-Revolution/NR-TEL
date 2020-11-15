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

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.posick.mdns.Lookup
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.DClass
import org.xbill.DNS.Type

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val accountDao = AccountDatabase.getDatabase(application).accountDao()
    private val repository = AccountRepository(accountDao)
    val allAccount = repository.allAccount

    val editAccount = MutableLiveData<AccountData>()
    var mdnsList = emptyArray<String>()

    private val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager?

    fun blank() {
        val emptyAccount = AccountData(0, 0, "", 0, false, "", "", 5060, "", "")
        editAccount.postValue(emptyAccount)
    }

    fun insert() {
        editAccount.value?.also {
            viewModelScope.launch(Dispatchers.IO) {
                val orderNum = allAccount.value?.size ?: 0
                val account = it.copy(itemId = 0, orderNumber = orderNum + 2)
                repository.insert(account)
                repository.shortViewModel()
            }
        }
    }

    fun update() {
        editAccount.value?.also {
            viewModelScope.launch(Dispatchers.IO) {
                repository.update(it)
            }
        }
    }

    fun delete() {
        editAccount.value?.also {
            viewModelScope.launch(Dispatchers.IO) {
                repository.delete(it)
                repository.shortViewModel()
            }
        }
    }

    fun updateActive(orderNum: Int, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAccountById(orderNum).also {
                repository.update(it.copy(accountActive = isChecked))
            }
        }
    }

    fun updateOutgoing(orderNum: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearOutgoing()
            repository.setOutgoing(orderNum)
        }
    }

    fun updateShort(oldOrderNum: Int, newOrderNum: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAccountById(oldOrderNum).also {
                repository.update(it.copy(orderNumber = newOrderNum))
                repository.shortViewModel()
            }
        }
    }

    fun getAccountById(orderNum: Int) {
        runBlocking {
            withContext(Dispatchers.IO) {
                repository.getAccountById(orderNum)
            }.also {
                editAccount.postValue(it)
            }
        }
    }

    fun setUriByMdns(IP: String) {
        editAccount.value?.also {
            val account = it.copy(serverUri = IP)
            editAccount.postValue(account)
        }
    }

    fun resolveMdns() {
        editAccount.value?.also { account ->
            runBlocking {
                mdnsList = withContext(Dispatchers.IO) {
                    var tempUri = emptyArray<String>()
                    try {
                        wifiManager?.createMulticastLock("mDnsLock4IPv6")?.also {
                            it.setReferenceCounted(true)
                            it.acquire()
                            for (record in Lookup(account.serverMdns, Type.AAAA, DClass.IN).lookupRecords()) {
                                if (record.type == Type.AAAA) {
                                    tempUri += (record as AAAARecord).address.hostAddress
                                }
                            }
                            it.release()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    tempUri
                }
            }
        }
    }
}
