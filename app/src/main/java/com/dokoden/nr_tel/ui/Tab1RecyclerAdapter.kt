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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dokoden.nr_tel.databinding.Tab1RecyclerItemBinding
import com.dokoden.nr_tel.model.CallLogDataClass

class Tab1RecyclerAdapter(
    private val listener: OnCardClickListener
) : RecyclerView.Adapter<Tab1RecyclerAdapter.ViewHolder>() {

    var dataList = emptyList<CallLogDataClass>()

    interface OnCardClickListener {
        fun onCardClicked(callLogDataClass: CallLogDataClass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        Tab1RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.also {
            it.itemData = dataList[position]
            it.itemClickListener = listener
            it.executePendingBindings()
        }
    }

    class ViewHolder(val binding: Tab1RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root)
}