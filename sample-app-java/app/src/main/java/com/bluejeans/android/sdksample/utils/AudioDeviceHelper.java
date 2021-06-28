/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.utils;

import com.bluejeans.bluejeanssdk.devices.AudioDevice;

public class AudioDeviceHelper {
    public static String getAudioDeviceName(AudioDevice audioDevice) {
        if (audioDevice == AudioDevice.BluetoothHeadset.INSTANCE) {
            return "BluetoothHeadset";
        } else if (audioDevice == AudioDevice.USBDevice.INSTANCE) {
            return "USBDevice";
        } else if (audioDevice == AudioDevice.USBHeadset.INSTANCE) {
            return "USBHeadset";
        } else if (audioDevice == AudioDevice.WiredHeadsetDevice.INSTANCE) {
            return "WiredHeadsetDevice";
        } else if (audioDevice == AudioDevice.WiredHeadPhones.INSTANCE) {
            return "WiredHeadPhones";
        } else if (audioDevice == AudioDevice.Speaker.INSTANCE) {
            return "Speaker";
        } else if (audioDevice == AudioDevice.Earpiece.INSTANCE) {
            return "Earpiece";
        }
        return "None";
    }
}
