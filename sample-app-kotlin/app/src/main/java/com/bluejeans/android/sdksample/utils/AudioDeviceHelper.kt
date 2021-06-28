/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.utils

import com.bluejeans.bluejeanssdk.devices.AudioDevice

class AudioDeviceHelper {
    companion object {
        fun getAudioDeviceName(audioDevice: AudioDevice?): String {
            return when (audioDevice) {
                AudioDevice.BluetoothHeadset -> {
                    "BluetoothHeadset"
                }
                AudioDevice.USBDevice -> {
                    "USBDevice"
                }
                AudioDevice.USBHeadset -> {
                    "USBHeadset"
                }
                AudioDevice.WiredHeadsetDevice -> {
                    "WiredHeadsetDevice"
                }
                AudioDevice.WiredHeadPhones -> {
                    "WiredHeadPhones"
                }
                AudioDevice.Speaker -> {
                    "Speaker"
                }
                AudioDevice.Earpiece -> {
                    "Earpiece"
                }
                else -> "None"
            }
        }
    }
}