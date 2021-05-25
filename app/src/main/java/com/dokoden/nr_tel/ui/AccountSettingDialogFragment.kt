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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.databinding.AccountDialogFragmentBinding
import com.dokoden.nr_tel.model.AccountViewModel

class AccountSettingDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val accountViewModel by activityViewModels<AccountViewModel>()

        val mainHostFragment =
            requireParentFragment().parentFragmentManager.findFragmentById(R.id.main_navi_host) as NavHostFragment

        val binding = DataBindingUtil.inflate<AccountDialogFragmentBinding>(
            LayoutInflater.from(requireActivity()),
            R.layout.account_dialog_fragment,
            null,
            false
        ).also {
            it.button.setOnClickListener {
                mainHostFragment.navController.navigate(R.id.accountMdnsSettingDialogFragment, null)
            }
        }

        val builder = AlertDialog
            .Builder(requireActivity())
            .setTitle(R.string.account_title)
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
            }

        accountViewModel.editAccount.observe(requireParentFragment()) {
            binding.viewModel = accountViewModel
            when {
                it.orderNumber == 0 -> {
                    builder
                        .setPositiveButton(R.string.save) { _, _ ->
                            accountViewModel.insert()
                        }
                }
                it.orderNumber >= 1 -> {
                    builder
                        .setPositiveButton(R.string.save) { _, _ ->
                            accountViewModel.update()
                        }
                        .setNegativeButton(R.string.delete) { _, _ ->
                            accountViewModel.delete()
                        }
                }
                else -> {
                }
            }
        }
        return builder.create()
    }
}
