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

import android.telecom.Call
import androidx.lifecycle.MutableLiveData
import com.dokoden.nr_tel.libsip.SipAccount

class TempRepository {
    companion object {
        val status = MutableLiveData("")
        val isOncall = MutableLiveData(false)

        val callName = MutableLiveData("")
        val callNumber = MutableLiveData("")
        val callStatus = MutableLiveData("")

        val callLog = MutableLiveData<List<CallLogDataClass>>()
        val contactBook = MutableLiveData<List<ContactBookDataClass>>()

        val sipAccountList = MutableLiveData<List<SipAccount>>()

        val telephoneCall = MutableLiveData<Call>()
        val isTelephone = MutableLiveData(true)
    }
}