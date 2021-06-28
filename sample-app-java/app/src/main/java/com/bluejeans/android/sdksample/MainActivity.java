/*
 * Copyright (c) 2020 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bjnclientcore.inmeeting.contentshare.ContentShareType;
import com.bluejeans.android.sdksample.menu.MenuFragment;
import com.bluejeans.android.sdksample.menu.MenuFragment.IMenuCallback;
import com.bluejeans.android.sdksample.menu.adapters.AudioDeviceAdapter;
import com.bluejeans.android.sdksample.menu.adapters.VideoDeviceAdapter;
import com.bluejeans.android.sdksample.menu.adapters.VideoLayoutAdapter;
import com.bluejeans.android.sdksample.participantlist.ParticipantListFragment;
import com.bluejeans.android.sdksample.viewpager.ScreenSlidePagerAdapter;
import com.bluejeans.bluejeanssdk.devices.AudioDevice;
import com.bluejeans.bluejeanssdk.devices.VideoDevice;
import com.bluejeans.bluejeanssdk.devices.VideoDeviceService;
import com.bluejeans.bluejeanssdk.logging.LoggingService;
import com.bluejeans.bluejeanssdk.meeting.ContentShareAvailability;
import com.bluejeans.bluejeanssdk.meeting.ContentShareState;
import com.bluejeans.bluejeanssdk.meeting.MeetingService;
import com.bluejeans.bluejeanssdk.permission.PermissionService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

import static com.bluejeans.android.sdksample.utils.AudioDeviceHelper.getAudioDeviceName;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    public static final int SCREEN_SHARE_REQUEST_CODE = 1;

    private final String appVersionString = "v" + SampleApplication.getBlueJeansSDK().getVersion();
    private final PermissionService mPermissionService = SampleApplication.getBlueJeansSDK().getPermissionService();
    private final MeetingService mMeetingService = SampleApplication.getBlueJeansSDK().getMeetingService();
    private final VideoDeviceService mVideoDeviceService = SampleApplication.getBlueJeansSDK().getVideoDeviceService();
    private final LoggingService mLoggingService = SampleApplication.getBlueJeansSDK().getLoggingService();

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
    private TextView mTvProgressMsg, mAppVersion;
    private Group mInMeetingControls;
    private ImageView mIvParticipant;
    private ImageView mIvScreenShare;
    private MenuFragment mBottomSheetFragment;
    private ParticipantListFragment mParticipantListFragment = null;

    //For alter dialog
    private VideoDeviceAdapter mVideoDeviceAdapter = null;
    private AudioDeviceAdapter mAudioDeviceAdapter = null;
    private VideoLayoutAdapter mVideoLayoutAdapter = null;
    private AlertDialog mAudioDialog = null;
    private AlertDialog mVideoLayoutDialog = null;
    private AlertDialog mVideoDeviceDialog = null;

    private int mZoomScaleFactor = 1; // default value of 1, no zoom to start with

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // permission service needs activity to be registered before calling request for permissions
        mPermissionService.register(this);
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
        Log.d(TAG, "onConfigurationChanged");

        /* The multi-stream remote video fragment computes size at run-time, when handling config change and using
        viewpager2, we need to make sure video fragment or video fragment's parent is visible on Config change inorder to
        propagate dimensions at runtime.*/
        mViewPager.setCurrentItem(0);
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
                mMeetingService.endMeeting();
                mVideoDeviceService.enableSelfVideoPreview(!mIsVideoMuted);
                endMeeting();
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
                    mVideoDeviceService.enableSelfVideoPreview(!mIsVideoMuted);
                }
                toggleVideoMuteUnMuteView(mIsVideoMuted);
                break;
            case R.id.imgRoster:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.rosterContainer, mParticipantListFragment)
                        .addToBackStack("ParticipantListFragment")
                        .commit();
                break;
            case R.id.imgScreenShare:
                if (mMeetingService.getContentShareService().getContentShareState().getValue() instanceof ContentShareState.Stopped) {
                    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREEN_SHARE_REQUEST_CODE);
                } else {
                    mMeetingService.getContentShareService().stopContentShare();
                }
                break;
            case R.id.ivCameraSettings:
                showCameraSettingsDialog();
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SCREEN_SHARE_REQUEST_CODE:
                if (data != null) {
                    mMeetingService.getContentShareService().startContentShare(new ContentShareType.Screen(data));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        subscribeForParticipants();
        subscribeForContentShareState();
        subscribeForContentShareAvailability();
        subscribeForContentShareEvents();
    }

    private void checkCameraPermissionAndStartSelfVideo() {
        if (mPermissionService.hasPermission(PermissionService.Permission.Camera.INSTANCE)) {
            startSelfVideo();
        } else {
            PermissionService.Permission[] arr = {PermissionService.Permission.Camera.INSTANCE};
            mDisposable.add(mPermissionService.requestPermissions(arr).subscribe(
                    grantedStatus -> {
                        if (grantedStatus == PermissionService.RequestStatus.Granted.INSTANCE) {
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
                .replace(R.id.selfViewFrame, mVideoDeviceService.getSelfVideoFragment())
                .commit();
        mVideoDeviceService.enableSelfVideoPreview(true);
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
        mDisposable.add(mPermissionService.requestAllPermissions().subscribe(
                areAllPermissionsGranted -> {
                    if (areAllPermissionsGranted == PermissionService.RequestStatus.Granted.INSTANCE) {
                        joinMeeting();
                    } else {
                        Log.i(TAG, "Not enough permissions to join a meeting");
                    }
                },
                err -> {
                    Log.e(TAG, "Error in requesting permissions subscription " + err.getMessage());
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
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            Log.i(TAG, "Join state is " + result);
                            if (result == MeetingService.JoinResult.Success.INSTANCE) {
                                // Explicitly apply the current AV mute states as per user
                                // expectation after join success.
                                // SDK does not provide a scope to apply mute states out of meeting
                                if (mIsAudioMuted) {
                                    mMeetingService.setAudioMuted(true);
                                }
                                if (mIsVideoMuted) {
                                    mMeetingService.setVideoMuted(true);
                                }
                                mVideoDeviceService.enableSelfVideoPreview(false);
                                OnGoingMeetingService.startService(this);
                            } else {
                                showOutOfMeetingView();
                            }
                        },
                        error -> {
                            showOutOfMeetingView();
                        }));

    }

    private void endMeeting() {
        OnGoingMeetingService.stopService(this);
        showOutOfMeetingView();
    }

    // Return Unit.INSTANCE; is needed for a kotlin java interop
    // Refer https://developer.android.com/kotlin/interop#lambda_arguments for more details
    private void subscribeForMeetingStatus() {
        mDisposable.add(mMeetingService.getMeetingState().subscribeOnUI(
                state -> {
                    Log.i(TAG, " Meeting state " + state);
                    if (state instanceof MeetingService.MeetingState.Connected) {
                        showInMeetingView();
                    } else if (state instanceof MeetingService.MeetingState.Idle) {
                        endMeeting();
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in meeting status subscription" + err.getMessage());
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
                    Log.e(TAG, "Error in video state subscription" + err.getMessage());
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
            Log.e(TAG, "Error in remote content subscription " + err.getMessage());
            return Unit.INSTANCE;
        }));
    }

    private void subscribeForAudioMuteStatus() {
        mDisposable.add(mMeetingService.getAudioMuted().subscribe(
                isMuted -> {
                    if (isMuted != null) {
                        // This could be due to local mute or remote mute
                        toggleAudioMuteUnMuteView(isMuted);
                    }
                    Log.i(TAG, " Audio Mute state " + isMuted);
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in audio mute status subscription");
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForVideoMuteStatus() {
        mDisposable.add(mMeetingService.getVideoMuted().subscribeOnUI(
                isMuted -> {
                    if (isMuted != null) {
                        // This could be due to local mute or remote mute
                        toggleVideoMuteUnMuteView(isMuted);
                    }

                    Log.i(TAG, " Video Mute state " + isMuted);
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in video mute status subscription " + err.getMessage());
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
                    Log.e(TAG, "Error in video layout subscription " + err.getMessage());
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForCurrentAudioDevice() {
        mDisposable.add(mMeetingService.getAudioDeviceService().getCurrentAudioDevice().subscribeOnUI(
                currentAudioDevice -> {
                    if (currentAudioDevice != null) {
                        mBottomSheetFragment.updateAudioDevice(getAudioDeviceName(currentAudioDevice));
                        updateCurrentAudioDeviceForAlertDialog(currentAudioDevice);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in current audio device subscription " + err.getMessage());
                    return Unit.INSTANCE;
                }));

    }

    private void subscribeForAudioDevices() {
        mDisposable.add(mMeetingService.getAudioDeviceService().getAudioDevices().subscribeOnUI(audioDevices -> {
                    if (audioDevices != null) {
                        mAudioDeviceAdapter.clear();
                        mAudioDeviceAdapter.addAll(audioDevices);
                        mAudioDeviceAdapter.notifyDataSetChanged();
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in audio devices subscription " + err.getMessage());
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForCurrentVideoDevice() {
        mDisposable.add(mVideoDeviceService.getCurrentVideoDevice().subscribeOnUI(
                currentVideoDevice -> {
                    if (currentVideoDevice != null) {
                        mBottomSheetFragment.updateVideoDevice(currentVideoDevice.getName());
                        updateCurrentVideoDeviceForAlertDialog(currentVideoDevice);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in current video device subscription " + err.getMessage());
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForParticipants() {
        mDisposable.add(mMeetingService.getParticipantsService().getParticipants().subscribeOnUI(
                participantList -> {
                    if (mParticipantListFragment != null && participantList != null) {
                        mParticipantListFragment.updateMeetingList(participantList);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in Participants subscription" + err.getMessage());
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForContentShareAvailability() {
        mDisposable.add(mMeetingService.getContentShareService().getContentShareAvailability().subscribeOnUI(
                contentShareAvailability -> {
                    if (contentShareAvailability instanceof ContentShareAvailability.Available) {
                        mIvScreenShare.setVisibility(View.VISIBLE);
                    } else {
                        mIvScreenShare.setVisibility(View.GONE);
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in content share availability" + err.getMessage());
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForContentShareState() {
        mDisposable.add(mMeetingService.getContentShareService().getContentShareState().subscribeOnUI(
                contentShareState -> {
                    if (contentShareState instanceof ContentShareState.Stopped) {
                        mIvScreenShare.setSelected(false);
                        MeetingNotificationUtility.updateNotificationMessage(this, getString(R.string.meeting_notification_message));
                    } else {
                        mIvScreenShare.setSelected(true);
                        MeetingNotificationUtility.updateNotificationMessage(this, getString(R.string.screen_share_notification_message));
                    }
                    return Unit.INSTANCE;
                },
                err -> {
                    Log.e(TAG, "Error in content share state subscription" + err.getMessage());
                    return Unit.INSTANCE;
                }));
    }

    private void subscribeForContentShareEvents() {
        mDisposable.add(mMeetingService.getContentShareService().getContentShareEvent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        contentShareEvent -> {
                            Log.i(TAG, "Content share event is " + contentShareEvent);
                        },
                        err -> {
                            Log.e(TAG, "Error in content share events subscription" + err.getMessage());
                        }));
    }

    private void subscribeForVideoDevices() {
        mDisposable.add(mVideoDeviceService.getVideoDevices().subscribeOnUI(videoDevices -> {
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
        mIvParticipant = findViewById(R.id.imgRoster);
        mIvScreenShare = findViewById(R.id.imgScreenShare);
        ImageView mCameraSettings = findViewById(R.id.ivCameraSettings);
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
        mAppVersion = findViewById(R.id.tvAppVersion);
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
        mIvParticipant.setOnClickListener(this);
        mIvScreenShare.setOnClickListener(this);
        mIvMic.setOnClickListener(this);
        mIvVideo.setOnClickListener(this);
        mCameraSettings.setOnClickListener(this);
        mBottomSheetFragment = new MenuFragment(mIOptionMenuCallback);
        mParticipantListFragment = new ParticipantListFragment();
        mVideoLayoutAdapter = getVideoLayoutAdapter();
        mVideoDeviceAdapter = getVideoDeviceAdapter(new ArrayList<>());
        mAudioDeviceAdapter = getAudioDeviceAdapter(new ArrayList<>());
        mAppVersion.setText(appVersionString);
    }

    private void showJoiningInProgressView() {
        mJoinLayout.setVisibility(View.GONE);
        mTvProgressMsg.setVisibility(View.VISIBLE);
        mTvProgressMsg.setText(getString(R.string.connectingState));
        mInMeetingControls.setVisibility(View.VISIBLE);
        mAppVersion.setVisibility(View.GONE);
    }

    private void showInMeetingView() {
        mAppVersion.setVisibility(View.GONE);
        mTvProgressMsg.setVisibility(View.GONE);
        mJoinLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);
        mInMeetingControls.setVisibility(View.VISIBLE);
        mIvParticipant.setVisibility(View.VISIBLE);
    }

    private void showOutOfMeetingView() {
        mViewPager.setVisibility(View.GONE);
        mViewPager.setCurrentItem(0);
        mTabLayout.setVisibility(View.GONE);
        mTvProgressMsg.setVisibility(View.GONE);
        mInMeetingControls.setVisibility(View.GONE);
        mJoinLayout.setVisibility(View.VISIBLE);
        mIvParticipant.setVisibility(View.GONE);
        mIvScreenShare.setVisibility(View.GONE);
        mAppVersion.setVisibility(View.VISIBLE);
        if (mBottomSheetFragment != null && mBottomSheetFragment.isAdded()) {
            mBottomSheetFragment.dismiss();
        }
        if (mParticipantListFragment != null && mParticipantListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(mParticipantListFragment).commit();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("ParticipantListFragment");
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        if (mAudioDialog != null && mAudioDialog.isShowing()) {
            mAudioDialog.dismiss();
        }
        if (mVideoDeviceDialog != null && mVideoDeviceDialog.isShowing()) {
            mVideoDeviceDialog.dismiss();
        }
        if (mVideoLayoutDialog != null && mVideoLayoutDialog.isShowing()) {
            mVideoLayoutDialog.dismiss();
        }
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
        int resID = isMuted ? R.drawable.mic_off_black : R.drawable.mic_on_black;
        mIvMic.setImageResource(resID);
    }

    private void toggleVideoMuteUnMuteView(boolean isMuted) {
        int resID = isMuted ? R.drawable.videocam_off_black : R.drawable.videocam_on_black;
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
                Log.i(TAG, "No one is sharing their video");
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
        mVideoLayoutDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.video_layouts))
                .setAdapter(mVideoLayoutAdapter,
                        (dialog, which) -> selectVideoLayout(which)).create();
        mVideoLayoutDialog.show();
    }

    private void showAudioDeviceDialog() {
        mAudioDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.audio_devices))
                .setAdapter(mAudioDeviceAdapter,
                        (dialog, which) -> selectAudioDevice(which)).create();
        mAudioDialog.show();
    }

    private void showVideoDeviceDialog() {
        mVideoDeviceDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.video_devices))
                .setAdapter(mVideoDeviceAdapter,
                        (dialog, which) -> selectVideoDevice(which)).create();
        mVideoDeviceDialog.show();
    }

    private void updateCurrentVideoDeviceForAlertDialog(VideoDevice videoDevice) {
        List<VideoDevice> videoDevices = mVideoDeviceService.getVideoDevices().getValue();
        if (videoDevices != null) {
            mVideoDeviceAdapter.updateSelectedPosition(videoDevices.indexOf(videoDevice));
        }
    }

    private void updateCurrentAudioDeviceForAlertDialog(AudioDevice currentAudioDevice) {
        List<AudioDevice> audioDevices = mMeetingService.getAudioDeviceService().getAudioDevices().getValue();
        if (audioDevices != null) {
            mAudioDeviceAdapter.updateSelectedPosition(audioDevices.indexOf(currentAudioDevice));
        }
    }

    private void updateCurrentVideoLayoutForAlertDialog(String videoLayoutName) {
        mVideoLayoutAdapter.updateSelectedPosition(videoLayoutOptionList().indexOf(videoLayoutName));
    }

    private VideoLayoutAdapter getVideoLayoutAdapter() {
        return new VideoLayoutAdapter(this, android.R.layout.simple_list_item_single_choice,
                videoLayoutOptionList());
    }

    private VideoDeviceAdapter getVideoDeviceAdapter(List<VideoDevice> videoDevices) {
        return new VideoDeviceAdapter(this, android.R.layout.simple_list_item_single_choice,
                videoDevices);
    }

    private AudioDeviceAdapter getAudioDeviceAdapter(List<AudioDevice> audioDevices) {
        return new AudioDeviceAdapter(this, android.R.layout.simple_list_item_single_choice,
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
        AudioDevice audioDevice = mAudioDeviceAdapter.getItem(position);
        mAudioDeviceAdapter.updateSelectedPosition(position);
        mMeetingService.getAudioDeviceService().selectAudioDevice(audioDevice);
    }

    private void selectVideoDevice(int position) {
        mVideoDeviceAdapter.updateSelectedPosition(position);
        VideoDevice videoDevice = mVideoDeviceAdapter.getItem(position);
        mVideoDeviceService.selectVideoDevice(videoDevice);
    }

    private void selectVideoLayout(int position) {
        mVideoLayoutAdapter.updateSelectedPosition(position);
        setVideoLayout(mVideoLayoutAdapter.getItem(position));
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showCameraSettingsDialog() {
        final AlertDialog.Builder cameraSettingDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(10);
        seek.setMin(1);
        seek.setProgress(mZoomScaleFactor);
        cameraSettingDialog.setTitle(getString(R.string.camera_setting_title));
        cameraSettingDialog.setView(seek);
        try {
            CameraCharacteristics cameraCharacteristics = getCurrentCameraCharacteristics();
            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mZoomScaleFactor = progress;
                    Rect activeRegion = cameraCharacteristics
                            .get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    mVideoDeviceService.setRepeatingCaptureRequest(CaptureRequest.SCALER_CROP_REGION,
                            getCropRegionForZoom(activeRegion, progress), null);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        cameraSettingDialog.create();
        cameraSettingDialog.show();
    }

    /**
     * It calculates the new crop region by finding out the delta between active camera region's
     * x and y coordinates and divide by zoom scale factor to get updated camera's region.
     *
     * @param cameraActiveRegion active area of the image sensor.
     * @param zoomFactor scale factor
     * @return Rect coordinates of crop region to be zoomed.
     */
    private Rect getCropRegionForZoom(Rect cameraActiveRegion, int zoomFactor) {
        int xCenter = cameraActiveRegion.width() / 2;
        int yCenter = cameraActiveRegion.height() / 2;
        int xDelta = (int) (0.5f * cameraActiveRegion.width() / zoomFactor);
        int yDelta = (int) (0.5f * cameraActiveRegion.height() / zoomFactor);
        return new Rect(xCenter - xDelta, yCenter - yDelta, xCenter + xDelta,
                yCenter + yDelta);
    }

    private CameraCharacteristics getCurrentCameraCharacteristics() throws CameraAccessException {
        VideoDevice currentVideoDevice = mVideoDeviceService.getCurrentVideoDevice().getValue();
        if (currentVideoDevice != null) {
            String cameraId = mVideoDeviceService.getCurrentVideoDevice().getValue().getId();
            CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            return cameraManager.getCameraCharacteristics(cameraId);
        } else {
            throw new IllegalStateException("No active camera device.");
        }
    }
}