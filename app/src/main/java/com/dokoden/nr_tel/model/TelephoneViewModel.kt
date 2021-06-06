package com.dokoden.nr_tel.model

import android.app.Application
import android.telecom.VideoProfile
import androidx.lifecycle.AndroidViewModel

class TelephoneViewModel(application: Application) : AndroidViewModel(application) {
    private val thisApplication = application
    val telephoneCall = TempRepository.telephoneCall
    val isTel = TempRepository.isTelephone

    fun answer() {
        telephoneCall.value?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        telephoneCall.value?.disconnect()
    }
}