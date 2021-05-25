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
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.dokoden.nr_tel.R
import com.dokoden.nr_tel.databinding.MainFragmentBinding
import com.dokoden.nr_tel.databinding.NavHeaderMainBinding
import com.dokoden.nr_tel.model.AccountViewModel
import com.dokoden.nr_tel.model.MainViewModel
import com.dokoden.nr_tel.service.EndlessService
import com.dokoden.nr_tel.utility.Constants
import com.google.android.material.tabs.TabLayout

class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mainViewModel by viewModels<MainViewModel>()
        val accountViewModel by activityViewModels<AccountViewModel>()
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        val mainHostFragment =
            requireParentFragment().parentFragmentManager.findFragmentById(R.id.main_navi_host) as NavHostFragment
        val tabNavHostFragment = childFragmentManager.findFragmentById(R.id.tab_navi_host) as NavHostFragment

//        val graph = tabNavHostFragment.navController.navInflater.inflate(R.navigation.tab_navigation)
//        graph.startDestination = R.id.tab1Fragment
//        tabNavHostFragment.navController.graph = graph
//        binding.tabLayout.getTabAt(1)?.select()

        accountViewModel.allAccount.observe(viewLifecycleOwner) {
            binding.navView.menu.clear()
            binding.navView.menu
                .add(0, Menu.FIRST, 0, R.string.menu_add_account)
                .setIcon(R.drawable.ic_add_circle_outline_black_24dp)
                .setOnMenuItemClickListener {
                    accountViewModel.blank()
                    mainHostFragment.navController.navigate(R.id.accountSettingDialogFragment, null)
                    return@setOnMenuItemClickListener true
                }

            it.forEach { acountData ->
                binding.navView.menu
                    .add(0, acountData.orderNumber, 0, "")
                    .setChecked(acountData.accountOutgoing == 1 || it.size == 1)
                    .setIcon(R.drawable.ic_indicator_circle)
                    .setOnMenuItemClickListener { menuItem ->
                        accountViewModel.updateOutgoing(menuItem.itemId)
                        return@setOnMenuItemClickListener true
                    }
                    .actionView = Switch(context).also { switch ->
                    switch.isChecked = acountData.accountActive
                    switch.setOnCheckedChangeListener { buttonView, isChecked ->
                        accountViewModel.updateActive(buttonView.id, isChecked)
                    }
                    switch.text = acountData.titleText
                    switch.setOnLongClickListener { view ->
                        accountViewModel.getAccountById(view.id)
                        mainHostFragment.navController.navigate(R.id.accountSettingDialogFragment, null)
                        return@setOnLongClickListener true
                    }
                }
            }
        }

        binding.also {
            it.viewModel = mainViewModel
            it.lifecycleOwner = viewLifecycleOwner
            DataBindingUtil.bind<NavHeaderMainBinding>(it.navView.getHeaderView(0))

            it.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.settingsAccount -> {
                        it.drawerLayout.openDrawer(GravityCompat.START)
                        return@setOnNavigationItemSelectedListener true
                    }
                    R.id.settingsPreferenceFragment -> {
                        mainHostFragment.navController.navigate(R.id.settingsPreferenceFragment, null)
                        return@setOnNavigationItemSelectedListener true
                    }
                    else -> return@setOnNavigationItemSelectedListener false
                }
            }

            it.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> {
                            tabNavHostFragment.navController.navigate(R.id.tab0Fragment, null)
                            it.fab.also { fab ->
                                fab.hide()
                                fab.setImageResource(R.drawable.ic_call_black_24dp)
                                fab.show()
                            }
                        }
                        1 -> {
                            tabNavHostFragment.navController.navigate(R.id.tab1Fragment, null)
                            it.fab.also { fab ->
                                fab.hide()
                                fab.setImageResource(R.drawable.ic_delete_black_24dp)
                                fab.show()
                            }
                        }
                        2 -> {
                            tabNavHostFragment.navController.navigate(R.id.tab2Fragment, null)
                            it.fab.hide()
                        }
                        3 -> {
                            tabNavHostFragment.navController.navigate(R.id.tab3Fragment, null)
                            it.fab.also { fab ->
                                fab.hide()
                                fab.setImageResource(R.drawable.ic_add_black_24dp)
                                fab.show()
                            }
                        }
                        else -> return
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            it.fab.setOnClickListener { view ->
                when (tabNavHostFragment.navController.currentDestination?.id) {
                    R.id.tab0Fragment -> {
                        Intent(view.context, EndlessService::class.java).also { intent ->
                            intent.action = Constants.Actions.CallOutgoing.name
                            view.context.startService(intent)
                        }
                    }
                    R.id.tab1Fragment -> {
                        view.context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
                    }
                    R.id.tab2Fragment -> {
                    }
                    R.id.tab3Fragment -> {
                    }
                }
            }
            return it.root
        }
    }
}
