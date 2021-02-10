/*
 * Copyright (c) 2020 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bluejeans.android.sdksample.menu.ItemViewClickListener;
import com.bluejeans.android.sdksample.menu.MenuFragment;
import com.bluejeans.android.sdksample.menu.MenuFragment.IMenuCallback;
import com.bluejeans.android.sdksample.menu.adapters.AudioDeviceAdapter;
import com.bluejeans.android.sdksample.menu.adapters.VideoDeviceAdapter;
import com.bluejeans.android.sdksample.menu.adapters.VideoLayoutAdapter;
import com.bluejeans.android.sdksample.viewpager.ScreenSlidePagerAdapter;
import com.bluejeans.bluejeanssdk.meeting.AudioDevice;
import com.bluejeans.bluejeanssdk.meeting.MeetingService;
import com.bluejeans.bluejeanssdk.permission.PermissionService;
import com.bluejeans.bluejeanssdk.selfvideo.VideoDevice;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;

import static com.bluejeans.android.sdksample.utils.AudioDeviceHelper.getAudioDeviceName;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    private final PermissionService mPermissionService = SampleApplication.getBlueJeansSDK().getPermissionService();
    private final MeetingService mMeetingService = SampleApplication.getBlueJeansSDK().getMeetingService();

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private boolean mIsAudioMuted, mIsVideoMuted;
    private MeetingService.VideoState mVideoState;
    private boolean mIsRemoteContentAvailable;

    //View IDs
    private ConstraintLayout mSelfView, mJoinLayout;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private ImageView mIvMic;
    private ImageView mIvVideo;
    private EditText mEtEventId, mEtPassCode, mEtName;
    private TextView mTvProgressMsg;
    private Group mInMeetingControls;
    private MenuFragment mBottomSheetFragment;

    //For alter dialog
    private VideoDeviceAdapter mVideoDeviceAdapter = null;
    private AudioDeviceAdapter mAudioDeviceAdapter = null;
    private VideoLayoutAdapter mVideoLayoutAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkCameraPermissionAndStartSelfVideo();
        activateSDKSubscriptions();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            configurePortraitView();
        } else {
            configureLandscapeView();
        }
        mMeetingService.setSelfVideoOrientation(this);
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        mBottomSheetFragment = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnJoin:
                checkMinimumPermissionsAndJoin();
                break;
            case R.id.imgClose:
                endMeeting();
                mMeetingService.enableSelfVideoPreview(!mIsVideoMuted);
                showOutOfMeetingView();
                break;
            case R.id.imgMenuOption:
                mBottomSheetFragment.show(getSupportFragmentManager(), mBottomSheetFragment.getTag());
                break;
            case R.id.ivMic:
                mIsAudioMuted = !mIsAudioMuted;
                mMeetingService.setAudioMuted(mIsAudioMuted);
                toggleAudioMuteUnMuteView(mIsAudioMuted);
                break;
            case R.id.ivVideo:
                mIsVideoMuted = !mIsVideoMuted;
                if (mMeetingService.getMeetingState().getValue() instanceof MeetingService.MeetingState.Connected) {
                    mMeetingService.setVideoMuted(mIsVideoMuted);
                } else {
                    mMeetingService.enableSelfVideoPreview(!mIsVideoMuted);
                }
                toggleVideoMuteUnMuteView(mIsVideoMuted);
                break;
            default:
                break;
        }
    }

    private void activateSDKSubscriptions() {
        subscribeForMeetingStatus();
        subscribeForVideoMuteStatus();
        subscribeForAudioMuteStatus();
        subscribeForVideoState();
        subscribeForRemoteContentState();
        subscribeForVideoLayout();
        subscribeForAudioDevices();
        subscribeForCurrentAudioDevice();
        subscribeForVideoDevices();
        subscribeForCurrentVideoDevice();
    }

    private void checkCameraPermissionAndStartSelfVideo() {
        if (mPermissionService.hasPermission(PermissionService.Permission.Camera.INSTANCE)) {
            startSelfVideo();
        } else {
            PermissionService.Permission[] arr = {PermissionService.Permission.Camera.INSTANCE};
            mDisposable.add(mPermissionService.requestPermissions(this, arr).subscribe(
                    isGranted -> {
                        if (isGranted) {
                            startSelfVideo();
                        } else {
                            Log.d(TAG, "Camera permission denied");
                        }
                    },
                    err -> {
                        Log.e(TAG, "Error in requesting permission subscription");
                    }));
        }
    }

    private void startSelfVideo() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.selfViewFrame, mMeetingService.getSelfVideoFragment())
                .commit();
        mMeetingService.enableSelfVideoPreview(true);
    }

    private void checkMinimumPermissionsAndJoin() {
        if (mPermissionService.hasMinimumPermissions()) {
            hideKeyboard();
            joinMeeting();
        } else {
            requestMinimumPermissionsAndJoin();
        }
    }

    private void requestMinimumPermissionsAndJoin() {
        mDisposable.add(mPermissionService.requestAllPermissions(this).subscribe(
                areAllPermissionsGranted -> {
                    if (areAllPermissionsGranted) {
                        joinMeeting();
                    } else {
                        Log.i(TAG, "Not enough permissions to join a meeting");
                    }
                },
                err -> {
                    Log.e(TAG, "Error in requesting permissions subscription");
                }));
    }

    private void joinMeeting() {
        String meetingId = mEtEventId.getText().toString();
        String passcode = mEtPassCode.getText().toString();
        String name = (TextUtils.isEmpty(mEtName.getText().toString()) ? "AndroidSDK"
                : mEtName.getText().toString());
        showJoiningInProgressView();

        mDisposable.add(mMeetingService.joinMeeting(
                new MeetingService.JoinParams
                        (meetingId, passcode, name))
                .subscribe(
                        result -> {
                            if (result == MeetingService.JoinResult.Success.INSTANCE) {
                                mMeetingService.setAudioMuted(mIsAudioMuted);
                                mMeetingService.setVideoMuted(mIsVideoMuted);
                                mMeetingService.enableSelfVideoPreview(false);
                            } else {
                                showOutOfMeetingView();
                            }
                        },
                        error -> {
                            showOutOfMeetingView();
                        }));

    }

    private void endMeeting() {
        mMeetingService.endMeeting();
    }

    // Return Unit.INSTANCE; is needed for a kotlin java interop
    // Refer https://developer.android.com/kotlin/interop#lambda_arguments for more details
    private void subscribeForMeetingStatus() {
        mDisposable.add(mMeetingService.getMeetingState().subscribeOnUI(
                state -> {
                    Log.i(TAG, " Meeting state " + state);
                    if (state instanceof MeetingService.MeetingState.Connected) {
                        showInMeetingView();
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in meeting status subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForVideoState() {
        mDisposable.add(mMeetingService.getVideoState().subscribeOnUI(
                state -> {
                    mVideoState = state;
                    if (mVideoState instanceof MeetingService.VideoState.Active) {
                        showInMeetingView();
                    } else {
                        handleVideoState();
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in video state subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForRemoteContentState() {
        mDisposable.add(mMeetingService.getReceivingRemoteContent().subscribeOnUI(isReceivingRemoteContent -> {
            if (isReceivingRemoteContent != null) {
                mIsRemoteContentAvailable = isReceivingRemoteContent;
                if (isReceivingRemoteContent) {
                    showInMeetingView();
                } else {
                    handleRemoteContentState();
                }
            }
            return Unit.INSTANCE;
        }, err -> {
            Log.e(TAG, "Error in remote content subscription");
            return Unit.INSTANCE;
        }));
    }

    private void subscribeForAudioMuteStatus() {
        mDisposable.add(mMeetingService.getAudioMuted().subscribe(
                isMuted -> {
                    // This could be due to local mute or remote mute
                    Log.i(TAG, " Audio Mute state " + isMuted);
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in audio mute status subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForVideoMuteStatus() {
        mDisposable.add(mMeetingService.getVideoMuted().subscribe(
                isMuted -> {
                    // This could be due to local mute or remote mute
                    Log.i(TAG, " Video Mute state " + isMuted);
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in video mute status subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForVideoLayout() {
        mDisposable.add(mMeetingService.getVideoLayout().subscribeOnUI(
                videoLayout -> {
                    if (videoLayout != null) {
                        String videoLayoutName = null;
                        if (videoLayout.equals(MeetingService.VideoLayout.Speaker.INSTANCE)) {
                            videoLayoutName = getString(R.string.speaker_view);
                        } else if (videoLayout.equals(MeetingService.VideoLayout.Gallery.INSTANCE)) {
                            videoLayoutName = getString(R.string.gallery_view);
                        } else if (videoLayout.equals(MeetingService.VideoLayout.People.INSTANCE)) {
                            videoLayoutName = getString(R.string.people_view);
                        }
                        mBottomSheetFragment.updateVideoLayout(videoLayoutName);
                        updateCurrentVideoLayoutForAlertDialog(videoLayoutName);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in video layout subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForCurrentAudioDevice() {
        mDisposable.add(mMeetingService.getCurrentAudioDevice().subscribeOnUI(
                currentAudioDevice -> {
                    if (currentAudioDevice != null) {
                        mBottomSheetFragment.updateAudioDevice(getAudioDeviceName(currentAudioDevice));
                        updateCurrentAudioDeviceForAlertDialog(currentAudioDevice);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in current audio device subscription");
                    return Unit.INSTANCE;
                }));

    }

    private void subscribeForAudioDevices() {
        mDisposable.add(mMeetingService.getAudioDevices().subscribeOnUI(audioDevices -> {
                    if (audioDevices != null) {
                        mAudioDeviceAdapter.clear();
                        mAudioDeviceAdapter.addAll(audioDevices);
                        mAudioDeviceAdapter.notifyDataSetChanged();
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in audio devices subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForCurrentVideoDevice() {
        mDisposable.add(mMeetingService.getCurrentVideoDevice().subscribeOnUI(
                currentVideoDevice -> {
                    if (currentVideoDevice != null) {
                        mBottomSheetFragment.updateVideoDevice(currentVideoDevice.getName());
                        updateCurrentVideoDeviceForAlertDialog(currentVideoDevice);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in current video device subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForVideoDevices() {
        mDisposable.add(mMeetingService.getVideoDevices().subscribeOnUI(videoDevices -> {
                    if (videoDevices != null) {
                        mVideoDeviceAdapter.clear();
                        mVideoDeviceAdapter.addAll(videoDevices);
                        mVideoDeviceAdapter.notifyDataSetChanged();
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in video devices subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void initViews() {
        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tabLayout);
        ImageView mIvClose = findViewById(R.id.imgClose);
        ImageView mIvMenuOption = findViewById(R.id.imgMenuOption);
        //Self View
        mSelfView = findViewById(R.id.selfView);
        mIvMic = findViewById(R.id.ivMic);
        mIvVideo = findViewById(R.id.ivVideo);
        //Join Layout
        mJoinLayout = findViewById(R.id.joinInfo);
        mEtEventId = findViewById(R.id.etEventId);
        mEtPassCode = findViewById(R.id.etPasscode);
        mEtName = findViewById(R.id.etName);
        //Progress View
        mTvProgressMsg = findViewById(R.id.tvProgressMsg);
        mInMeetingControls = findViewById(R.id.optionGroup);
        // Initialize adapter and click listener
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.registerOnPageChangeCallback(new PagerChangeCallback());
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
        }).attach();

        Button btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(this);
        mIvClose.setOnClickListener(this);
        mIvMenuOption.setOnClickListener(this);
        mIvMic.setOnClickListener(this);
        mIvVideo.setOnClickListener(this);
        mBottomSheetFragment = new MenuFragment(mIOptionMenuCallback);
        mVideoLayoutAdapter = getVideoLayoutAdapter();
        mVideoDeviceAdapter = getVideoDeviceAdapter(new ArrayList<>());
        mAudioDeviceAdapter = getAudioDeviceAdapter(new ArrayList<>());
    }

    private void showJoiningInProgressView() {
        mJoinLayout.setVisibility(View.GONE);
        mTvProgressMsg.setVisibility(View.VISIBLE);
        mTvProgressMsg.setText(getString(R.string.connectingState));
        mInMeetingControls.setVisibility(View.VISIBLE);
    }

    private void showInMeetingView() {
        mTvProgressMsg.setVisibility(View.GONE);
        mJoinLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);
        mInMeetingControls.setVisibility(View.VISIBLE);
    }

    private void showOutOfMeetingView() {
        mViewPager.setVisibility(View.GONE);
        mViewPager.setCurrentItem(0);
        mTabLayout.setVisibility(View.GONE);
        mTvProgressMsg.setVisibility(View.GONE);
        mInMeetingControls.setVisibility(View.GONE);
        mJoinLayout.setVisibility(View.VISIBLE);
    }

    private void showProgress(String msg) {
        mTvProgressMsg.setVisibility(View.VISIBLE);
        mTvProgressMsg.setText(msg);
    }

    private void hideProgress() {
        mTvProgressMsg.setVisibility(View.GONE);
        mTvProgressMsg.setText("");
    }

    private void toggleAudioMuteUnMuteView(boolean isMuted) {
        int resID = isMuted ? R.drawable.ic_mic_off_black_24dp : R.drawable.ic_mic_black_24dp;
        mIvMic.setImageResource(resID);
    }

    private void toggleVideoMuteUnMuteView(boolean isMuted) {
        int resID = isMuted ? R.drawable.ic_videocam_off_black_24dp : R.drawable.ic_videocam_black_24dp;
        mIvVideo.setImageResource(resID);
    }

    private void configurePortraitView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mSelfView.getLayoutParams();
        params.startToStart = R.id.parent_layout;
        params.topToTop = R.id.parent_layout;
        params.endToEnd = R.id.parent_layout;
        params.dimensionRatio = getResources().getString(R.string.self_view_ratio);
        mSelfView.requestLayout();
    }

    private void configureLandscapeView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mSelfView.getLayoutParams();
        params.startToStart = ConstraintLayout.LayoutParams.UNSET;
        params.topToTop = R.id.parent_layout;
        params.endToEnd = R.id.parent_layout;
        params.dimensionRatio = getResources().getString(R.string.self_view_ratio);
        mSelfView.requestLayout();
    }

    private void handleRemoteContentState() {
        if (mViewPager.getVisibility() == View.VISIBLE) {
            if (mIsRemoteContentAvailable) {
                hideProgress();
            } else {
                showProgress("No one is sharing the remote content.");
            }
        }
    }

    private void handleVideoState() {
        if (mViewPager.getVisibility() == View.VISIBLE) {
            if (mVideoState instanceof MeetingService.VideoState.Active) {
                hideProgress();
            } else if (mVideoState instanceof MeetingService.VideoState.Inactive.SingleParticipant) {
                showProgress("You are the only participant. Please wait some one to join.");
            } else if (mVideoState instanceof MeetingService.VideoState.Inactive.NoOneHasVideo) {
                showProgress("No one is sharing their video");
            } else if (mVideoState instanceof MeetingService.VideoState.Inactive.NeedsModerator) {
                showProgress("Need moderator");
            } else {
                hideProgress();
            }
        }
    }

    private class PagerChangeCallback extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == 0) {
                handleVideoState();
            } else if (position == 1) {
                handleRemoteContentState();
            }
        }
    }

    private final IMenuCallback mIOptionMenuCallback =
            new IMenuCallback() {
                @Override
                public void showVideoLayoutView(String videoLayoutName) {
                    updateCurrentVideoLayoutForAlertDialog(videoLayoutName);
                    showVideoLayoutDialog(videoLayoutName);
                }

                @Override
                public void showAudioDeviceView() {
                    showAudioDeviceDialog();
                }

                @Override
                public void showVideoDeviceView() {
                    showVideoDeviceDialog();
                }
            };

    private ArrayList<String> videoLayoutOptionList() {
        ArrayList<String> videoLayoutList = new ArrayList<>();
        videoLayoutList.add(getString(R.string.people_view));
        videoLayoutList.add(getString(R.string.speaker_view));
        videoLayoutList.add(getString(R.string.gallery_view));
        return videoLayoutList;
    }

    private void showVideoLayoutDialog(String videoLayoutName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.video_layouts));
        ItemViewClickListener itemViewClickListener = this::selectVideoLayout;
        mVideoLayoutAdapter.setItemViewClickListener(itemViewClickListener);
        builder.setAdapter(mVideoLayoutAdapter, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAudioDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.audio_devices));
        ItemViewClickListener itemViewClickListener = this::selectAudioDevice;
        mAudioDeviceAdapter.setItemViewClickListener(itemViewClickListener);
        builder.setAdapter(mAudioDeviceAdapter, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showVideoDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.video_devices));
        ItemViewClickListener itemViewClickListener = this::selectVideoDevice;
        mVideoDeviceAdapter.setItemViewClickListener(itemViewClickListener);
        builder.setAdapter(mVideoDeviceAdapter, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCurrentVideoDeviceForAlertDialog(VideoDevice videoDevice) {
        List<VideoDevice> videoDevices = mMeetingService.getVideoDevices().getValue();
        mVideoDeviceAdapter.updateSelectedPosition(videoDevices.indexOf(videoDevice));
    }

    private void updateCurrentAudioDeviceForAlertDialog(AudioDevice currentAudioDevice) {
        List<AudioDevice> audioDevices = mMeetingService.getAudioDevices().getValue();
        mAudioDeviceAdapter.updateSelectedPosition(audioDevices.indexOf(currentAudioDevice));
    }

    private void updateCurrentVideoLayoutForAlertDialog(String videoLayoutName) {
        mVideoLayoutAdapter.updateSelectedPosition(videoLayoutOptionList().indexOf(videoLayoutName));
    }

    private VideoLayoutAdapter getVideoLayoutAdapter() {
        return new VideoLayoutAdapter(this, R.layout.adapter_item_view,
                videoLayoutOptionList());
    }

    private VideoDeviceAdapter getVideoDeviceAdapter(List<VideoDevice> videoDevices) {
        return new VideoDeviceAdapter(this, R.layout.adapter_item_view,
                videoDevices);
    }

    private AudioDeviceAdapter getAudioDeviceAdapter(List<AudioDevice> audioDevices) {
        return new AudioDeviceAdapter(this, R.layout.adapter_item_view,
                audioDevices);
    }

    private void setVideoLayout(String videoLayoutName) {
        MeetingService.VideoLayout videoLayout = null;
        if (videoLayoutName.equals(getString(R.string.people_view))) {
            videoLayout = MeetingService.VideoLayout.People.INSTANCE;
        } else if (videoLayoutName.equals(getString(R.string.gallery_view))) {
            videoLayout = MeetingService.VideoLayout.Gallery.INSTANCE;
        } else if (videoLayoutName.equals(getString(R.string.speaker_view))) {
            videoLayout = MeetingService.VideoLayout.Speaker.INSTANCE;
        }
        if (videoLayout != null) {
            mMeetingService.setVideoLayout(videoLayout);
        }
    }

    private void selectAudioDevice(int position) {
        AudioDevice audioDevice = (AudioDevice) mAudioDeviceAdapter.getItem(position);
        mMeetingService.selectAudioDevice(audioDevice);
    }

    private void selectVideoDevice(int position) {
        VideoDevice videoDevice = (VideoDevice) mVideoDeviceAdapter.getItem(position);
        mMeetingService.selectVideoDevice(videoDevice);
    }

    private void selectVideoLayout(int position) {
        setVideoLayout(mVideoLayoutAdapter.getItem(position));
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}