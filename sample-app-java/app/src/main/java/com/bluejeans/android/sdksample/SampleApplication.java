/*
 * Copyright (c) 2020 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample;

import android.app.Application;

import com.bluejeans.bluejeanssdk.BlueJeansSDK;
import com.bluejeans.bluejeanssdk.BlueJeansSDKInitParams;

public class SampleApplication extends Application {

    private static BlueJeansSDK blueJeansSDK;

    @Override
    public void onCreate() {
        super.onCreate();
        initSDK();
    }

    private void initSDK() {
        try {
            blueJeansSDK = new BlueJeansSDK(new BlueJeansSDKInitParams(this));
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static BlueJeansSDK getBlueJeansSDK() {
        return blueJeansSDK;
    }
}
