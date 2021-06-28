/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView

/** Type helper used for the callback triggered once our view has been bound */
typealias BindCallback<T> = (view: View, data: T, position: Int) -> Unit

class MenuItemAdapter<T>(
    context: Context,
    private val itemLayoutId: Int,
    private val dataset: List<T>,
    private val onBind: BindCallback<T>
) : ArrayAdapter<T>(context, itemLayoutId, dataset) {
    private var selectedPosition = 0

    inner class ViewHolder(view: View) {
        var checkedTextView: CheckedTextView = view.findViewById(android.R.id.text1)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var newView = convertView
        val viewHolder: ViewHolder
        if (newView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = inflater.inflate(itemLayoutId, parent, false)
            viewHolder = ViewHolder(newView)
            newView.tag = viewHolder
        } else (newView.tag as MenuItemAdapter<T>.ViewHolder).also { viewHolder = it }
        viewHolder.checkedTextView.isChecked = (position == selectedPosition)
        onBind(viewHolder.checkedTextView, dataset[position], position)
        return newView!!
    }

    fun updateSelectedPosition(selectedItemPosition: Int) {
        this.selectedPosition = selectedItemPosition
        notifyDataSetChanged()
    }
}