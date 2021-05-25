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

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dokoden.nr_tel.databinding.MainActivityBinding
import com.dokoden.nr_tel.service.EndlessService
import com.dokoden.nr_tel.utility.Constants
import net.taptappun.taku.kobayashi.runtimepermissionchecker.RuntimePermissionChecker

class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private lateinit var binding: MainActivityBinding
    private val REQUEST_PERMISSION = 100
    private val REQUEST_ROLE = 200
    private val REQUEST_TELECOM = 300

    private val navController by lazy { findNavController(R.id.main_navi_host) }
    private val endlessService by lazy { Intent(this, EndlessService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //パーミッションの許可
        offerReplacingDefaultDialer()
        RuntimePermissionChecker.requestAllPermissions(this, Constants.RequestCode.Permission.ordinal)

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && PermissionChecker.PERMISSION_GRANTED in grantResults) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ROLE -> when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    //the user didn't set you as the default screening app...
                }
                Activity.RESULT_OK -> {
                    //The user set you as the default screening app!
                }
                Activity.RESULT_FIRST_USER -> {

                }
            }
            REQUEST_TELECOM -> when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    //the user didn't set you as the default screening app...
                }
                Activity.RESULT_OK -> {
                    //The user set you as the default screening app!
                }
                Activity.RESULT_FIRST_USER -> {

                }
            }
            else -> {
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
            Constants.Actions.CallIncoming.name -> navController.navigate(R.id.callIncommingFragment, null)
            Constants.Actions.CallAnswer.name -> navController.navigate(R.id.callFragment, null)
            Constants.Actions.CallReject.name -> navController.navigate(R.id.mainFragment, null)
            Constants.Actions.CallOutgoing.name -> navController.navigate(R.id.callFragment, null)
            Constants.Actions.CallXfer.name -> navController.navigate(R.id.callFragment, null)
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
                startActivityForResult(intent, REQUEST_ROLE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                startActivityForResult(intent, REQUEST_TELECOM)
            }
        }
    }
}
