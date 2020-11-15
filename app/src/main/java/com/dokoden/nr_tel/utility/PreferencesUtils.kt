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

package com.dokoden.nr_tel.utility

import android.content.Context
import androidx.preference.PreferenceManager
import com.dokoden.nr_tel.R

class PreferencesUtils(val context: Context) {
    private fun resString(stringId: Int) = context.resources.getString(stringId)
    private fun prefString(key: Int, defValue: Int) =
        PreferenceManager.getDefaultSharedPreferences(context).getString(resString(key), resString(defValue))!!

    private fun prefBoolean(key: Int, defValue: Int) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(resString(key), resString(defValue).toBoolean())

    val logLevel = prefString(R.string.log_level_key, R.string.log_level_default).toLong()
    val consoleLevel = prefString(R.string.console_level_key, R.string.console_level_default).toLong()
    val logUseDirectFile = prefString(R.string.log_use_direct_file_key, R.string.log_use_direct_file_default)
    val logFileFlags = prefString(R.string.log_file_flags_key, R.string.log_file_flags_default)

    val userAgent = prefString(R.string.user_agent_key, R.string.user_agent_default)
    val uaThreadCount = prefString(R.string.ua_thread_count_key, R.string.ua_thread_count_default).toLong()

    val mediaThreadCount = prefString(R.string.media_thread_count_key, R.string.media_thread_count_default).toLong()
    val mediaQuality = prefString(R.string.media_quality_key, R.string.media_quality_default).toLong()
    val echoMode = prefString(R.string.echo_mode_key, R.string.echo_mode_default).toLong()
    val echoCancellationTail =
        prefString(R.string.echo_cancellation_tail_key, R.string.echo_cancellation_tail_default).toLong()

    val timerMinSESec =
        prefString(R.string.callConfig_timerMinSESec_key, R.string.callConfig_timerMinSESec_default).toLong()
    val timerSessExpiresSec = prefString(
        R.string.callConfig_timerSessExpiresSec_key,
        R.string.callConfig_timerSessExpiresSec_default
    ).toLong()
    val iceEnabled = prefBoolean(R.string.natConfig_iceEnabled_key, R.string.natConfig_iceEnabled_default)
    val autoShowIncoming =
        prefBoolean(R.string.videoConfig_autoShowIncoming_key, R.string.videoConfig_autoShowIncoming_default)
    val autoTransmitOutgoing =
        prefBoolean(R.string.videoConfig_autoTransmitOutgoing_key, R.string.videoConfig_autoTransmitOutgoing_default)
}