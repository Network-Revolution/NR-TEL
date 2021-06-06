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
import com.dokoden.nr_tel.databinding.Tab2FragmentBinding
import com.dokoden.nr_tel.model.ContactBookDataClass
import com.dokoden.nr_tel.model.MainViewModel
import com.dokoden.nr_tel.service.EndlessService
import com.dokoden.nr_tel.utility.Constants

class Tab2Fragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mainViewModel by viewModels<MainViewModel>()
        val recyclerAdapter = Tab2RecyclerAdapter(object : Tab2RecyclerAdapter.OnCardClickListener {
            override fun onCardClicked(contactBookDataClass: ContactBookDataClass) {
                mainViewModel.callNumber.postValue(contactBookDataClass.number)
                Intent(activity, EndlessService::class.java).also {
                    it.action = if (mainViewModel.isOncall.value!!)
                        Constants.Actions.TransferCall.name else Constants.Actions.OutgoingCall.name

                    container!!.context.startService(it)
                }
            }
        })

        mainViewModel.loadContactBook()

        mainViewModel.contactBook.observe(viewLifecycleOwner) {
            it?.also {
                recyclerAdapter.dataList = it
                recyclerAdapter.notifyDataSetChanged()
            }
        }

        Tab2FragmentBinding.inflate(inflater, container, false).also {
            it.contactBookAdapter = recyclerAdapter
            return it.root
        }
    }
}
