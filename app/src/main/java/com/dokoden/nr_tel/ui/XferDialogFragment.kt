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
import androidx.navigation.fragment.NavHostFragment
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.databinding.XferDialogFragmentBinding
import com.dokoden.nr_tel.model.MainViewModel
import com.google.android.material.tabs.TabLayout

class XferDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainViewModel by viewModels<MainViewModel>()

        val binding = DataBindingUtil.inflate<XferDialogFragmentBinding>(
            LayoutInflater.from(requireActivity()),
            R.layout.xfer_dialog_fragment,
            null,
            false
        )

        val navHostFragment =
            requireParentFragment().parentFragmentManager.findFragmentById(R.id.tab_dialog_navi_host) as NavHostFragment
        val navController = navHostFragment.navController
        val graph = navController.navInflater.inflate(R.navigation.tab_navigation)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                graph.startDestination = when (tab.position) {
                    0 -> R.id.tab0Fragment
                    1 -> R.id.tab1Fragment
                    2 -> R.id.tab2Fragment
                    3 -> R.id.tab3Fragment
                    else -> return
                }
                navController.graph = graph
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        mainViewModel.callNumber.observe(requireParentFragment(), {
            binding.viewModel = mainViewModel
        })

        return AlertDialog
            .Builder(requireActivity())
            .setTitle(R.string.incall_transfer_title)
            .setView(binding.root)
            .setPositiveButton(R.string.done) { _, _ ->
            }
            .setNeutralButton(R.string.cancel) { _, _ ->
            }
            .create()
    }
}
