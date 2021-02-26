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
import com.bluejeans.bluejeanssdk.meeting.AudioDevice;

import java.util.List;

import static com.bluejeans.android.sdksample.utils.AudioDeviceHelper.getAudioDeviceName;

public class AudioDeviceAdapter extends ArrayAdapter<AudioDevice> {
    private final Context mContext;
    private final int mResource;
    private int selectedPosition = -1;
    private ItemViewClickListener mItemViewClickListener;

    public AudioDeviceAdapter(@NonNull Context context, int resource, @NonNull List<AudioDevice> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final AudioDeviceViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,
                    parent, false);
            viewHolder = new AudioDeviceViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AudioDeviceViewHolder) convertView.getTag();
        }

        AudioDevice audioDevice = getItem(position);
        viewHolder.rbItemMenu.setText(getAudioDeviceName(audioDevice));

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

    class AudioDeviceViewHolder {
        RadioButton rbItemMenu;

        public AudioDeviceViewHolder(View view) {
            rbItemMenu = (RadioButton) view.findViewById(R.id.rbItemMenu);
        }
    }

    @Nullable
    @Override
    public AudioDevice getItem(int position) {
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
