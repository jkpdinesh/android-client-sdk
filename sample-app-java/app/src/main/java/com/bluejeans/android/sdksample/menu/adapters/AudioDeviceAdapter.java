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

import com.bluejeans.bluejeanssdk.devices.AudioDevice;

import java.util.List;

import static com.bluejeans.android.sdksample.utils.AudioDeviceHelper.getAudioDeviceName;

public class AudioDeviceAdapter extends ArrayAdapter<AudioDevice> {
    private final Context mContext;
    private final int mResource;
    private int selectedPosition = -1;

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
            convertView = LayoutInflater.from(mContext).inflate(mResource,
                    parent, false);
            viewHolder = new AudioDeviceViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AudioDeviceViewHolder) convertView.getTag();
        }

        AudioDevice audioDevice = getItem(position);
        viewHolder.rbItemMenu.setText(getAudioDeviceName(audioDevice));
        viewHolder.rbItemMenu.setChecked(position == selectedPosition);
        return convertView;
    }

    static class AudioDeviceViewHolder {
        CheckedTextView rbItemMenu;

        public AudioDeviceViewHolder(View view) {
            rbItemMenu = view.findViewById(android.R.id.text1);
        }
    }

    public void updateSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }
}
