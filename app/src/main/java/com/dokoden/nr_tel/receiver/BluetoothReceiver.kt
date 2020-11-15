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

package com.dokoden.nr_tel.receiver

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class BluetoothReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (intent?.action) {
            BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_DISCONNECTED
                )
                when (state) {
                    BluetoothHeadset.STATE_CONNECTED -> {
//                                Log.d(LOG_TAG, "Bluetooth headset is connected")
//                                if (audioFocusUsage != -1) {
//                                    // Without delay, SCO_AUDIO_STATE_CONNECTING ->
//                                    // SCO_AUDIO_STATE_DISCONNECTED
//                                    Timer("Sco", false).schedule(1000) {
//                                        Log.d(LOG_TAG, "Starting Bluetooth SCO")
//                                        audioManager.startBluetoothSco()
//                                    }
//                                }
                    }
                    BluetoothHeadset.STATE_DISCONNECTED -> {
//                                Log.d(LOG_TAG, "Bluetooth headset is disconnected")
                        if (audioManager.isBluetoothScoOn) {
//                                    Log.d(LOG_TAG, "Stopping Bluetooth SCO")
                            audioManager.stopBluetoothSco()
                        }
                    }

                }
            }
            BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                )
                when (state) {
                    BluetoothHeadset.STATE_AUDIO_CONNECTED -> {
//                                Log.d(LOG_TAG, "Bluetooth headset audio is connected")
                    }
                    BluetoothHeadset.STATE_AUDIO_DISCONNECTED -> {
//                                Log.d(LOG_TAG, "Bluetooth headset audio is disconnected")
                        if (audioManager.isBluetoothScoOn) {
//                                    Log.d(LOG_TAG, "Stopping Bluetooth SCO")
                            audioManager.stopBluetoothSco()
                        }
                    }
                }
            }
            AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                val state = intent.getIntExtra(
                    AudioManager.EXTRA_SCO_AUDIO_STATE,
                    AudioManager.SCO_AUDIO_STATE_DISCONNECTED
                )
                when (state) {
                    AudioManager.SCO_AUDIO_STATE_CONNECTING -> {
//                                Log.d(LOG_TAG, "Bluetooth headset SCO is connecting")
                    }
                    AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
//                                Log.d(LOG_TAG, "Bluetooth headset SCO is connected")
                    }
                    AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
//                                Log.d(LOG_TAG, "Bluetooth headset SCO is disconnected")
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                    if (audioFocusRequest != null) {
//                                        if (audioManager.abandonAudioFocusRequest(audioFocusRequest!!) ==
//                                            AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                                            Log.d(LOG_TAG, "Audio focus abandoned")
//                                            audioFocusRequest = null
//                                            audioFocusUsage = -1
//                                        } else {
//                                            Log.d(LOG_TAG, "Failed to abandon audio focus")
//                                        }
//                                    }
//                                } else {
//                                    if (audioFocusUsage != -1) {
//                                        if (audioManager.abandonAudioFocus(null) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                                            Log.d(LOG_TAG, "Audio focus abandoned")
//                                            audioFocusUsage = -1
//                                        } else {
//                                            Log.d(LOG_TAG, "Failed to abandon audio focus")
//                                        }
//                                    }
//                                }
                    }
                    AudioManager.SCO_AUDIO_STATE_ERROR -> {
//                                Log.d(LOG_TAG, "Bluetooth headset SCO state ERROR")
                    }
                }
            }
        }
    }
}