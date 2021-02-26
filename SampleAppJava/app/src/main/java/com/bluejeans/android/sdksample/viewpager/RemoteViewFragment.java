/*
 * Copyright (c) 2020 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.viewpager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bluejeans.android.sdksample.R;
import com.bluejeans.android.sdksample.SampleApplication;

public class RemoteViewFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remote_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.remoteView, SampleApplication.getBlueJeansSDK().getMeetingService().getRemoteVideoFragment())
                .commit();
    }
}
