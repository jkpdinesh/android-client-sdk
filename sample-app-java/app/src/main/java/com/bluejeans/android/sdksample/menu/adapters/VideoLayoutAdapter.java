/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.menu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluejeans.android.sdksample.R;
import com.bluejeans.android.sdksample.menu.ItemViewClickListener;

import java.util.List;

public class VideoLayoutAdapter extends ArrayAdapter<String> {

    private final Context mContext;
    private final int mResource;
    private int selectedPosition = -1;
    private ItemViewClickListener mItemViewClickListener;

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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,
                    parent, false);
            viewHolder = new VideoLayoutViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoLayoutViewHolder) convertView.getTag();
        }

        viewHolder.rbItemMenu.setText(getItem(position));

        viewHolder.rbItemMenu.setChecked(position == selectedPosition);

        viewHolder.rbItemMenu.setOnClickListener(view -> {
            selectedPosition = position;
            notifyDataSetChanged();
            if (mItemViewClickListener != null) {
                mItemViewClickListener.onItemClickListener(position);
            }
        });
        return convertView;
    }

    class VideoLayoutViewHolder {
        RadioButton rbItemMenu;

        public VideoLayoutViewHolder(View view) {
            rbItemMenu = (RadioButton) view.findViewById(R.id.rbItemMenu);
        }
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }

    public void updateSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    public void setItemViewClickListener(ItemViewClickListener itemViewClickListener) {
        this.mItemViewClickListener = itemViewClickListener;
    }
}
