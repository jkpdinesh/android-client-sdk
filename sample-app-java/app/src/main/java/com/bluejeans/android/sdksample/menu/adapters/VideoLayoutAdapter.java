/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.menu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class VideoLayoutAdapter extends ArrayAdapter<String> {

    private final Context mContext;
    private final int mResource;
    private int selectedPosition = -1;

    public VideoLayoutAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final VideoLayoutViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource,
                    parent, false);
            viewHolder = new VideoLayoutViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoLayoutViewHolder) convertView.getTag();
        }
        viewHolder.rbItemMenu.setText(getItem(position));
        viewHolder.rbItemMenu.setChecked(position == selectedPosition);
        return convertView;
    }

    static class VideoLayoutViewHolder {
        CheckedTextView rbItemMenu;

        public VideoLayoutViewHolder(View view) {
            rbItemMenu = view.findViewById(android.R.id.text1);
        }
    }

    public void updateSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }
}
