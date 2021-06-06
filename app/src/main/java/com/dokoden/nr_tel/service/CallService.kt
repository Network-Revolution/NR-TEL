package com.dokoden.nr_tel.service

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import androidx.lifecycle.ViewModelProvider
import com.dokoden.nr_tel.MainActivity
import com.dokoden.nr_tel.model.TelephoneViewModel
import com.dokoden.nr_tel.utility.Constants

class CallService : InCallService() {
    private val viewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory(application).create(TelephoneViewModel::class.java)
    }

    override fun onCallAdded(call: Call) {
        viewModel.telephoneCall.postValue(call)
        Intent(this, MainActivity::class.java).let {
            it.action = when (call.state) {
                Call.STATE_NEW -> Constants.Actions.TelephoneNew.name
                Call.STATE_DIALING -> Constants.Actions.TelephoneDialing.name
                Call.STATE_RINGING -> Constants.Actions.TelephoneRinging.name
                Call.STATE_HOLDING -> Constants.Actions.TelephoneHolding.name
                Call.STATE_ACTIVE -> Constants.Actions.TelephoneActive.name
                Call.STATE_DISCONNECTED -> Constants.Actions.TelephoneDisconnected.name
                Call.STATE_SELECT_PHONE_ACCOUNT -> Constants.Actions.TelephoneSelectPhoneAccount.name
                Call.STATE_CONNECTING -> Constants.Actions.TelephoneConnecting.name
                Call.STATE_DISCONNECTING -> Constants.Actions.TelephoneDisconnecting.name
                Call.STATE_PULLING_CALL -> Constants.Actions.TelephonePullingCall.name
                Call.STATE_AUDIO_PROCESSING -> Constants.Actions.TelephoneAudioProcessing.name
                Call.STATE_SIMULATED_RINGING -> Constants.Actions.TelephoneSimulatedRinging.name
                else -> Constants.Actions.Kill.name
            }
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }
    }

    override fun onCallRemoved(call: Call) {
        viewModel.telephoneCall.postValue(call)
        Intent(this, MainActivity::class.java).let {
            it.action = Constants.Actions.DeclineIncomingCall.name
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }
    }
}