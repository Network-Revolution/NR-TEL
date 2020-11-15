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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.model.AccountViewModel

class AccountMdnsSettingDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val accountViewModel by activityViewModels<AccountViewModel>()
        accountViewModel.resolveMdns()

        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.account_title)
            .setItems(accountViewModel.mdnsList) { dialog, which ->
                accountViewModel.setUriByMdns(accountViewModel.mdnsList[which])
            }
            .setNeutralButton(R.string.cancel) { _, _ ->
            }
            .create()
    }
}
