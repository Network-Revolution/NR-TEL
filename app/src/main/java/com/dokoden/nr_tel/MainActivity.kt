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

package com.dokoden.nr_tel

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dokoden.nr_tel.databinding.MainActivityBinding
import com.dokoden.nr_tel.service.EndlessService
import com.dokoden.nr_tel.utility.Constants

class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private lateinit var binding: MainActivityBinding
    private val navController by lazy { findNavController(R.id.main_navi_host) }
    private val endlessService by lazy { Intent(this, EndlessService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //パーミッションの許可
        offerReplacingDefaultDialer()

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        onNewIntent(intent)
        if (!EndlessService.isServiceRunning) {
            endlessService.let {
                it.action = Constants.Actions.Start.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            }
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?, pref: Preference?): Boolean {
        // Identify the Navigation Destination
        val navDestination = navController.graph.find { pref?.fragment!!.endsWith(it.label ?: "") }
        // Navigate to the desired destination
        navDestination?.let { navController.navigate(it.id) }
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val action = intent?.action ?: return
        when (action) {
            "android.intent.action.MAIN" -> {
                return
            }
            Constants.Actions.TelephoneNew.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneDialing.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneRinging.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneHolding.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneActive.name -> navController.navigate(R.id.callFragment)
            Constants.Actions.TelephoneDisconnected.name -> navController.navigate(R.id.mainFragment)
            Constants.Actions.TelephoneSelectPhoneAccount.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneConnecting.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneDisconnecting.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephonePullingCall.name -> navController.navigate(R.id.callFragment)
            Constants.Actions.TelephoneAudioProcessing.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.TelephoneSimulatedRinging.name -> navController.navigate(R.id.callIncommingFragment)
            Constants.Actions.DeclineIncomingCall.name -> navController.navigate(R.id.mainFragment)

            Constants.Actions.IncomingCallScreen.name -> navController.navigate(R.id.callIncommingFragment, null)
            Constants.Actions.AcceptIncomingCall.name -> navController.navigate(R.id.callFragment, null)
            Constants.Actions.DeclineIncomingCall.name -> navController.navigate(R.id.mainFragment, null)
            Constants.Actions.Kill.name -> navController.navigate(R.id.mainFragment, null)
            else -> {

            }
        }
    }

    private fun offerReplacingDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            if (!isHeld) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                requestPermissions(intent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                requestPermissions(intent)
            }
        }
    }

    private fun requestPermissions(intent: Intent) {
        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Toast.makeText(this, "${it.key} = ${it.value}", Toast.LENGTH_LONG).show()
                }
            }
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {

                }
                Activity.RESULT_CANCELED -> {
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.WAKE_LOCK,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.WRITE_CALL_LOG,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                        )
                    )
                }
                Activity.RESULT_FIRST_USER -> {

                }
            }
        }
        startForResult.launch(intent)
    }
}
