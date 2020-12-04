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

package com.dokoden.nr_tel.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.*
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import com.dokoden.nr_tel.MainActivity
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.libsip.SipStackViewModel
import com.dokoden.nr_tel.model.MainViewModel
import com.dokoden.nr_tel.utility.Constants
import java.util.*

class EndlessService : LifecycleService() {
    private val viewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory(application).create(MainViewModel::class.java)
    }

    private val sipStack by lazy {
        ViewModelProvider.AndroidViewModelFactory(application).create(SipStackViewModel::class.java)
    }

    private var ringToneTimer: Timer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusUsage = -1
    private var origVolumes = arrayOf(-1, -1, -1, -1)
    private var activeNetwork = ""

    private lateinit var notifyManager: NotificationManager
    private lateinit var notifyBuilder: NotificationCompat.Builder
    private val defaultChannelID by lazy { "$packageName.channel_default" }
    private val callChannelID by lazy { "$packageName.channel_call" }
    private val notifyTitle by lazy { resources.getString(R.string.notification_title) }
    private val notifyText by lazy { resources.getString(R.string.notification_text) }

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private val vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private val ringTone by lazy {
        RingtoneManager.getActualDefaultRingtoneUri(applicationContext, RingtoneManager.TYPE_RINGTONE).let {
            RingtoneManager.getRingtone(applicationContext, it)
        }
    }

    private lateinit var partialWakeLock: PowerManager.WakeLock
    private val partialTag by lazy { "$packageName:partial_wakeTag" }
    private lateinit var proximityWakeLock: PowerManager.WakeLock
    private val proximityTag by lazy { "$packageName:proximity_wakeTag" }
    private lateinit var wifiLock: WifiManager.WifiLock
    private val wifiTag by lazy { "$packageName:wifi_tag" }

    companion object {
        var isServiceRunning = false
        var isServiceClean = false
        var speakerPhone = false
        var callVolume = 0
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sipStack.initLib()

        (getSystemService(Context.POWER_SERVICE) as PowerManager).also {
            partialWakeLock = it.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, partialTag)
            proximityWakeLock = it.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, proximityTag)
        }

        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, wifiTag)

        viewModel.enabledAccount.observe(this, {
            sipStack.delAllAccount()
            sipStack.addAllAccount(it)

            val inboxStyle = NotificationCompat.InboxStyle()
            it.forEach { accountData ->
                inboxStyle.addLine(accountData.titleText)
            }

            notifyBuilder.setStyle(inboxStyle)
            notifyManager.notify(Constants.DefaultNotifyID, notifyBuilder.build())
        })

        sipStack.status.observe(this, {
            when (sipStack.status.value) {
                Constants.Actions.CallIncoming.name -> incomingCall()
                Constants.Actions.CallReject.name -> callReject()
            }
        })

        val builder = NetworkRequest.Builder()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    sipStack.handleNetworkChange()
                }
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = intent?.action ?: Constants.Actions.Start.name
        when (action) {
            Constants.Actions.Start.name -> startService()
            Constants.Actions.Stop.name -> stopService()
            Constants.Actions.StopForce.name -> stopService()
            Constants.Actions.Kill.name -> onDestroy()
            Constants.Actions.CallAnswer.name -> callAnswer()
            Constants.Actions.CallReject.name -> callReject()
            Constants.Actions.CallOutgoing.name -> callOutgoing()
            Constants.Actions.CallXfer.name -> callXfer()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        sipStack.deInitLib()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private fun startService() {
        createDefaultNotify()
        partialWakeLock.acquire()
    }

    private fun stopService() {
        if (audioManager.isBluetoothScoOn) audioManager.stopBluetoothSco()
        if (this::notifyManager.isInitialized) notifyManager.cancelAll()
        if (this::partialWakeLock.isInitialized && partialWakeLock.isHeld) partialWakeLock.release()
        if (this::proximityWakeLock.isInitialized && proximityWakeLock.isHeld) proximityWakeLock.release()
        if (this::wifiLock.isInitialized && wifiLock.isHeld) wifiLock.release()
        stopForeground(true)
        stopSelf()
        sipStack.deInitLib()
    }

    private fun createDefaultNotify() {
        notifyManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(defaultChannelID, "Default", NotificationManager.IMPORTANCE_LOW).also {
                it.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                it.setSound(null, null)
                notifyManager.createNotificationChannel(it)
            }
            notifyBuilder = NotificationCompat.Builder(this, defaultChannelID)
        } else {
            notifyBuilder = NotificationCompat.Builder(this).setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        val pendingIntent = Intent(this, MainActivity::class.java).let {
            it.action = Constants.Actions.Start.name
            PendingIntent.getActivity(this, Constants.RequestCode.Default.ordinal, it, 0)
        }

        notifyBuilder
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
//            .setUsesChronometer(true)
            .setContentTitle(notifyTitle)
            .setContentText(notifyText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_indicator_circle)
            .color = resources.getColor(R.color.colorLightGreen, theme)
        startForeground(Constants.DefaultNotifyID, notifyBuilder.build())
    }

    private fun createCallNotify(callerId: String, callerStatus: String) {
        val customView = RemoteViews(packageName, R.layout.call_notification).also {
            it.setTextViewText(R.id.callerName, callerId)
            it.setTextViewText(R.id.callerStateus, callerStatus)
//            it.setImageViewBitmap(R.id.photo, callerImage.bitmap)
        }

        val pendingIntent = Intent(this, MainActivity::class.java).let {
            it.action = Constants.Actions.CallIncoming.name
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            it.putExtra(R.id.callerName, callerId)
//            it.putExtra(R.id.callerStateus, callerStatus)
            PendingIntent.getActivity(this, Constants.RequestCode.Call.ordinal, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val answerPendingIntent = Intent(this, EndlessService::class.java).let {
            it.action = Constants.Actions.CallAnswer.name
            PendingIntent.getService(this, Constants.RequestCode.Call.ordinal, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val rejectPendingIntent = Intent(this, EndlessService::class.java).let {
            it.action = Constants.Actions.CallReject.name
            PendingIntent.getService(this, Constants.RequestCode.Call.ordinal, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            NotificationChannel(callChannelID, "IncomingCall", NotificationManager.IMPORTANCE_HIGH).also {
                it.setSound(null, null)
                notificationManager.createNotificationChannel(it)
            }

            customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent)
            customView.setOnClickPendingIntent(R.id.btnDecline, rejectPendingIntent)

            NotificationCompat.Builder(this, callChannelID)
                .setFullScreenIntent(pendingIntent, true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(customView)
                .setCustomBigContentView(customView)
        } else {
            val answerAction = NotificationCompat
                .Action
                .Builder(R.drawable.ic_call_black_24dp, "HANG OUT", answerPendingIntent)
                .build()
            val hangupAction = NotificationCompat
                .Action
                .Builder(R.drawable.ic_call_end_black_24dp, "HANG UP", rejectPendingIntent)
                .build()
            NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_call_black_24dp))
                .setContentIntent(pendingIntent)
                .addAction(answerAction)
                .addAction(hangupAction)
        }

        notification
            .setContentTitle(getString(R.string.app_name))
            .setTicker("Call_STATUS")
            .setContentText("IncomingCall")
            .setSmallIcon(R.drawable.ic_call_black_24dp)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setVibrate(null)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)

        notifyManager.notify(Constants.CallNotifyID, notification.build())
    }

    private fun incomingCall() {
        val callerId = sipStack.callNumber.value ?: return
        val callerStatus = sipStack.callStatus.value ?: return
//        val callerImage = getDrawable(intent?.getIntExtra("caller_image", R.drawable.ic_person_black_200dp)!!) as BitmapDrawable

        startRinging()
        createCallNotify(callerId, callerStatus)
    }

    private fun callAnswer() {
        notifyManager.cancel(Constants.CallNotifyID)
        stopRinging()
        sipStack.callAnswer()
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.Actions.CallAnswer.name
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }
    }

    private fun callReject() {
        notifyManager.cancel(Constants.CallNotifyID)
        stopRinging()
        sipStack.callReject()
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.Actions.CallReject.name
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }
    }

    private fun callOutgoing() {
        val outgoingNumber = viewModel.callNumber.value ?: return
        sipStack.callOutgoing(outgoingNumber)
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.Actions.CallOutgoing.name
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }

        viewModel.callNumber.postValue("")
    }

    private fun callXfer() {
        val outgoingNumber = viewModel.callNumber.value ?: return
        sipStack.callTransfer(outgoingNumber)
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.Actions.CallOutgoing.name
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }

        viewModel.callNumber.postValue("")
    }

    private fun startRinging() {
        audioManager.mode = AudioManager.MODE_RINGTONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringTone.isLooping = true
            ringTone.play()
        } else {
            ringTone.play()
            ringToneTimer = Timer()
            ringToneTimer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (!ringTone.isPlaying) {
                        ringTone.play()
                    }
                }
            }, 1000, 1000)
        }

        val timings = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, 1)
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            vibrator.vibrate(effect, audioAttrs)
        } else {
            vibrator.vibrate(timings, 1)
        }
    }

    private fun stopRinging() {
        if (audioManager.mode == AudioManager.MODE_RINGTONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringTone.stop()
            } else if (ringToneTimer != null) {
                ringToneTimer!!.cancel()
                ringToneTimer = null
            }
        }
        vibrator.cancel()
    }
}
