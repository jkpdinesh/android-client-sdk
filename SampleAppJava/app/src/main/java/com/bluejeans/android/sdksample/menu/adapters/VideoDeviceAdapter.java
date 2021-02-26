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
import com.bluejeans.bluejeanssdk.selfvideo.VideoDevice;

import java.util.List;

public class VideoDeviceAdapter extends ArrayAdapter<VideoDevice> {
    private final Context mContext;
    private final int mResource;
    private int selectedPosition = -1;
    private ItemViewClickListener mItemViewClickListener;

    public VideoDeviceAdapter(@NonNull Context context, int resource, @NonNull List<VideoDevice> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final VideoDeviceViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,
                    parent, false);
            viewHolder = new VideoDeviceViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoDeviceViewHolder) convertView.getTag();
        }

        VideoDevice videoDevice = getItem(position);
        viewHolder.rbItemMenu.setText(videoDevice.getName());

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

    class VideoDeviceViewHolder {
        RadioButton rbItemMenu;

        public VideoDeviceViewHolder(View view) {
            rbItemMenu = (RadioButton) view.findViewById(R.id.rbItemMenu);
        }
    }

    @Nullable
    @Override
    public VideoDevice getItem(int position) {
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
