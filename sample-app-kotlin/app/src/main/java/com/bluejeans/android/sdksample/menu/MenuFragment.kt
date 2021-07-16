/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluejeans.android.sdksample.databinding.FragmentOptionMenuDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuFragment(private val menuCallBack: IMenuCallback) : BottomSheetDialogFragment() {

    private var videoLayout = ""
    private var currentAudioDevice = ""
    private var currentVideoDevice = ""
    private var menuFragmentBinding: FragmentOptionMenuDialogBinding? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    interface IMenuCallback {
        fun showVideoLayoutView(videoLayoutName: String)
        fun showAudioDeviceView()
        fun showVideoDeviceView()
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior = BottomSheetBehavior.from(requireView().parent as View)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        menuFragmentBinding = FragmentOptionMenuDialogBinding.inflate(inflater, container, false)
        return menuFragmentBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun updateVideoLayout(videoLayout: String?) {
        if (videoLayout != null) {
            this.videoLayout = videoLayout
        }
        updateView()
    }

    fun updateAudioDevice(currentAudioDevice: String?) {
        this.currentAudioDevice = currentAudioDevice!!
        updateView()
    }

    fun updateVideoDevice(currentVideoDevice: String?) {
        this.currentVideoDevice = currentVideoDevice!!
        updateView()
    }

    private fun initViews() {
        menuFragmentBinding?.mbVideoLayout?.setOnClickListener {
            menuCallBack.showVideoLayoutView(menuFragmentBinding!!.mbVideoLayout.text as String)
            dismiss()
        }
        menuFragmentBinding?.mbAudioDevice?.setOnClickListener {
            menuCallBack.showAudioDeviceView()
            dismiss()
        }

        menuFragmentBinding?.mbVideoDevice?.setOnClickListener {
            menuCallBack.showVideoDeviceView()
            dismiss()
        }
        updateView()
    }

    private fun updateView() {
        menuFragmentBinding?.let {
            it.mbVideoLayout.text = videoLayout
            it.mbAudioDevice.text = currentAudioDevice
            it.mbVideoDevice.text = currentVideoDevice
        }
    }
}