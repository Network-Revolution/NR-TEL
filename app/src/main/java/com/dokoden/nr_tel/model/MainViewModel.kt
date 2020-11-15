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

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.media.AudioManager
import android.media.ToneGenerator
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.service.EndlessService
import com.dokoden.nr_tel.utility.Constants
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val thisApplication = application

    private val accountDao = AccountDatabase.getDatabase(application).accountDao()
    private val repository = AccountRepository(accountDao)
    val enabledAccount = repository.enabledAccount
    val outgoingAccount = repository.outgoingAccount
    val callNumber = TempRepository.callNumber
    val callLog = TempRepository.callLog
    val contactBook = TempRepository.contactBook

    fun onClickButton(view: View) {
        callNumber.value += when (view.id) {
            R.id.button0 -> "0"
            R.id.button1 -> "1"
            R.id.button2 -> "2"
            R.id.button3 -> "3"
            R.id.button4 -> "4"
            R.id.button5 -> "5"
            R.id.button6 -> "6"
            R.id.button7 -> "7"
            R.id.button8 -> "8"
            R.id.button9 -> "9"
            R.id.button_star -> "*"
            R.id.button_pound -> "#"
            else -> ""
        }
    }

    fun onLongClickButton(view: View): Boolean {
        callNumber.value += when (view.id) {
            R.id.button0 -> "+"
            else -> callNumber.value
        }
        return false
    }

    fun onClickDelete() {
        if (callNumber.value!!.isNotEmpty()) {
            callNumber.value = callNumber.value!!.substring(0, callNumber.value!!.length - 1)
        }
    }

    fun onLongClickDelete(): Boolean {
        callNumber.value = ""
        return false
    }

    fun loadCallLog() {
        if (ContextCompat.checkSelfPermission(thisApplication, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_PHOTO_URI,
        )
        val cursor = thisApplication.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            CallLog.Calls.DEFAULT_SORT_ORDER
        ) ?: return

        if (cursor.count == 0) return

        cursor.moveToFirst()
        val tempListCallLog = mutableListOf<CallLogDataClass>()
        while (!cursor.isAfterLast) {
            val callType = when (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))) {
                CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> "INCOMMING"
                CallLog.Calls.MISSED_TYPE -> "MISSED CALL"
                else -> "UNKNOWN"
            }
            val callDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
            val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(Date(callDate))

            tempListCallLog += CallLogDataClass(
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) ?: "",
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)) ?: "",
                callType,
                dateString,
                cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION)).toString() + "sec",
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)) ?: ""
            )
            cursor.moveToNext()
        }
        cursor.close()
        callLog.postValue(tempListCallLog)
    }

    fun loadContactBook() {
        if (ContextCompat.checkSelfPermission(thisApplication, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
        )
        val cursor = thisApplication.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.STARRED} = 1",
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY
        ) ?: return

        if (cursor.count == 0) return

        cursor.moveToFirst()
        val tempListCallFavorite = mutableListOf<ContactBookDataClass>()
        while (!cursor.isAfterLast) {
            tempListCallFavorite += ContactBookDataClass(
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "",
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: "",
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) ?: "",
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)) ?: ""
            )
            cursor.moveToNext()
        }
        cursor.close()
        contactBook.postValue(tempListCallFavorite)
    }

    fun onClickMute(view: View) {
    }

    fun onClickSpeaker(view: View) {
    }

    fun onClickXferCall(view: View) {
    }

    fun onClickHold(view: View) {
        ToneGenerator(AudioManager.STREAM_VOICE_CALL, ToneGenerator.MAX_VOLUME).also {
            it.startTone(ToneGenerator.TONE_SUP_CONFIRM) //PBXで保留音
        }
    }

    fun onClickVideo(view: View) {
    }

    fun onCallAction(view: View) {
        Intent(view.context, EndlessService::class.java).also {
            it.action = when (view.id) {
                R.id.answerFab -> Constants.Actions.CallAnswer.name
                R.id.rejectFab -> Constants.Actions.CallReject.name
                else -> return
            }
            view.context.startService(it)
        }
    }
}