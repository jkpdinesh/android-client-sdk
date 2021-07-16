/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluejeans.android.sdksample.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class MenuFragment extends BottomSheetDialogFragment {
    private final IMenuCallback mIMenuCallback;
    private MaterialButton mMbVideoLayout, mMbAudioDevice, mMbVideoDevice;
    private String videoLayout = "";
    private String currentAudioDevice = "";
    private String currentVideoDevice = "";

    public interface IMenuCallback {
        void showVideoLayoutView(String videoLayoutName);

        void showAudioDeviceView();

        void showVideoDeviceView();
    }

    public MenuFragment(IMenuCallback iMenuCallback) {
        mIMenuCallback = iMenuCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_option_menu_dialog, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) requireView().getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    public void updateVideoLayout(String videoLayout) {
        this.videoLayout = videoLayout;
        updateView();
    }

    public void updateAudioDevice(String currentAudioDevice) {
        this.currentAudioDevice = currentAudioDevice;
        updateView();
    }

    public void updateVideoDevice(String currentVideoDevice) {
        this.currentVideoDevice = currentVideoDevice;
        updateView();
    }

    private void initViews(View view) {
        mMbVideoLayout = view.findViewById(R.id.mbVideoLayout);
        mMbAudioDevice = view.findViewById(R.id.mbAudioDevice);
        mMbVideoDevice = view.findViewById(R.id.mbVideoDevice);
        mMbVideoLayout.setOnClickListener(view1 -> {
            mIMenuCallback.showVideoLayoutView(mMbVideoLayout.getText().toString());
            dismiss();
        });
        mMbAudioDevice.setOnClickListener(view1 -> {
            mIMenuCallback.showAudioDeviceView();
            dismiss();
        });
        mMbVideoDevice.setOnClickListener(view1 -> {
            mIMenuCallback.showVideoDeviceView();
            dismiss();
        });
        updateView();
    }

    private void updateView() {
        if (mMbVideoLayout != null) {
            mMbVideoLayout.setText(videoLayout);
        }
        if (mMbAudioDevice != null) {
            mMbAudioDevice.setText(currentAudioDevice);
        }
        if (mMbVideoDevice != null) {
            mMbVideoDevice.setText(currentVideoDevice);
        }
    }
}
