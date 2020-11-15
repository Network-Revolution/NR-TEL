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

package com.dokoden.nr_tel.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.databinding.ToneDialogFragmentBinding
import com.dokoden.nr_tel.model.MainViewModel

class ToneDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainViewModel by viewModels<MainViewModel>()

        val binding = DataBindingUtil.inflate<ToneDialogFragmentBinding>(
            LayoutInflater.from(requireActivity()),
            R.layout.tone_dialog_fragment,
            null,
            false
        )

        mainViewModel.callNumber.observe(requireParentFragment(), {
            binding.viewModel = mainViewModel
        })

        return AlertDialog
            .Builder(requireActivity())
            .setTitle(R.string.incall_dtmf_title)
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
            }
            .create()
    }
}
