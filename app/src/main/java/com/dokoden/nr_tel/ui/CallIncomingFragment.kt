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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.databinding.CallIncomingBinding
import com.dokoden.nr_tel.model.MainViewModel
import com.dokoden.nr_tel.service.EndlessService
import com.dokoden.nr_tel.utility.Constants

class CallIncomingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mainViewModel by viewModels<MainViewModel>()
        val mainHostFragment =
            requireParentFragment().parentFragmentManager.findFragmentById(R.id.main_navi_host) as NavHostFragment

        CallIncomingBinding.inflate(inflater, container, false).also {
            it.viewModel = mainViewModel
            it.lifecycleOwner = viewLifecycleOwner
            it.answerFab.setOnClickListener {
                Intent(requireActivity(), EndlessService::class.java).also { intent ->
                    intent.action = Constants.Actions.AcceptIncomingCall.name
                    requireActivity().startService(intent)
                }
                mainHostFragment.navController.navigate(R.id.callFragment, null)
            }
            it.rejectFab.setOnClickListener {
                Intent(requireActivity(), EndlessService::class.java).also { intent ->
                    intent.action = Constants.Actions.DeclineIncomingCall.name
                    requireActivity().startService(intent)
                }
                mainHostFragment.navController.navigate(R.id.mainFragment, null)
            }
            return it.root
        }
    }
}
