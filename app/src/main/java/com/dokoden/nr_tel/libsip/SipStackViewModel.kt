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

package com.dokoden.nr_tel.libsip

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.provider.CallLog
import android.telephony.TelephonyManager
import androidx.lifecycle.AndroidViewModel
import com.dokoden.nr_tel.model.*
import com.dokoden.nr_tel.utility.Constants
import com.dokoden.nr_tel.utility.PreferencesUtils
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class SipStackViewModel(application: Application) : AndroidViewModel(application) {
    private val accountDao = AccountDatabase.getDatabase(application).accountDao()
    private val repository = AccountRepository(accountDao)
    val outgoingAccount = repository.outgoingAccount

    val status = TempRepository.status
    val callNumber = TempRepository.callNumber
    val callStatus = TempRepository.callStatus

    private val pref = PreferencesUtils(application.applicationContext)
    private val telephonyManager by lazy { application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }
    private val contentResolver = application.applicationContext.contentResolver


    var outgoingCall: SipCall? = null
    var currentSipCall: SipCall? = null
    var currentCall: CurrentCallDataClass? = null

    companion object {
        val sipAccountList = mutableListOf<SipAccount>()
    }

    fun initLib() {

    }

    fun deInitLib() {
        Runtime.getRuntime().gc()

    }

    fun sipObserver() {
        SipAppController(object : SipAppController.SipAppObserver {

        })
    }

    fun handleNetworkChange() {

    }

    fun callAnswer() {

    }

    fun callReject() {

    }

    fun callOutgoing(outgoingNumber: String) {
        val call = outgoingCall ?: return
        val outgoingAccountData = outgoingAccount.value?.get(0) ?: return
        val outgoingUri = "sip:${outgoingNumber}@[${outgoingAccountData.serverUri}]:${outgoingAccountData.serverPort}"
        currentSipCall = call
    }

    fun callXfer(outgoingNumber: String) {
        val call = outgoingCall ?: return
        val outgoingAccountData = outgoingAccount.value?.get(0) ?: return
        val outgoingUri = "sip:${outgoingNumber}@[${outgoingAccountData.serverUri}]:${outgoingAccountData.serverPort}"
    }

    fun callTransfer(outgoingNumber: String) {
        val call = outgoingCall ?: return
        val outgoingAccountData = outgoingAccount.value?.get(0) ?: return
        val outgoingUri = "sip:${outgoingNumber}@[${outgoingAccountData.serverUri}]:${outgoingAccountData.serverPort}"

    }

    fun delAllAccount() {
        sipAccountList.forEach { sipAccount ->

        }
        sipAccountList.clear()
    }

    fun addAllAccount(accountList: List<AccountData>) {
        accountList.forEach {

        }
    }

    fun sendTone(dtmf: Long) {
    }
}
