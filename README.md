[![BlueJeans Android Software Development Kit](https://user-images.githubusercontent.com/23289872/127987669-3842046b-2f08-46e4-9949-6bf0cdb45d95.png "BlueJeans Android Software Development Kit")](https://www.bluejeans.com "BlueJeans Android Software Development Kit")

# BlueJeans Android Client Software Development Kit

The BlueJeans Android Client Software Development Kit (SDK) gives a quick and easy way to bring immersive video-calling experience into your android applications.

With BlueJeans Android Client SDK, participants can join video conference meetings where they receive individual video streams from each of the video participant in the meeting. This provides an enhanced remote video quality experience with the resolution, fps of individual streams better as compared to a single composited stream in an earlier hybrid model.

## Features:
- Audio and Video Permission handling
- Join, End Meeting
- Self Video
- Remote Video, Remote Video states
- Content receive 
- Audio and Video self mute
- Orientation handling
- Video device enumeration, Selection
- Audio device enumeration, Selection
- Video Layout switch
- Participant list
- Participant properties: Audio mute state, Video mute state, is Self, Name and Unique Identifier
- Self Participant
- Screen Share
- Log Upload
- Multi stream support (Sequin Video Layouts)
- Enable torch / flash unit on a device
- Set capture requests such as zoom, exposure on the active video device
- Public and Private meeting Chat
- Remote Video and Content mute
- Meeting Information (Title, Host name, URL) property

## Current Version: 1.0.0

## Pre-requisites:
- **Android API level:** Min level 26

- **Android Device:**
   - OS level - Oreo 8.0 or later
   - CPU - armeabi-v7a, arm64-v8a
   - No support for emulator yet

- **Android Project & Gradle Settings:**
   - Android X
   - Compile SDK Version: 28 and above
   - Source and Target compatibility to java version 1_8 in gradle
   - RxJava, RxKotlin
   

## API Architecture
<img width="870" alt="BJNAndroidClientSDKArch" src="https://user-images.githubusercontent.com/23289872/127135069-e7558cd6-e326-43ad-8341-4507b9303933.png">


## SDK Documentation:
Detailed documentation of SDK functions is available [here](https://bluejeans.github.io/android-client-sdk)

## How it all works?
You can experience BlueJeans meetings using the android client SDK by following below 2 steps -

### Generate a meeting ID :
As a pre requiste to using the BlueJeans Android Client SDK to join meetings, you need to have a BlueJeans meeting ID. If you do not have a meeting ID then you can create one using a meeting schedule option using a BlueJeans account as below
   - Sign up for a BlueJeans Account either by opting in for a [trial](https://www.bluejeans.com/free-video-conferencing-trial) or a [paid mode](https://store.bluejeans.com/)
   - Once account is created, you can schedule a meeting either by using the account or through the [direct API](https://bluejeans.github.io/api-rest-howto/schedule.html) calls. In order to enable API calls on your account, please reach out to [support team](https://support.bluejeans.com/s/contactsupport).

### Integrate BlueJeans Android Client SDK
Integrate the SDK using the below guidelines and use SDK APIs to join a meeting using the generated meeting ID. 

## Integration Steps:
### Override Minimum SDK Version:
This version of BlueJeans Android Client SDK is compatible with Android version "26" or higher. Therefore, your Android app must have a minimum SDK version 26 or higher.

If your app runs with min SDK version below API level 26, you must override min SDK version as in the below sample. However please note that, SDK instantiation will fail if the app runs on API level below 26, please add build check to avoid SDK instantiation on device with API level < 26.

**Sample Code:** Add the below to your AndroidManifest.xml
```xml
<uses-sdk android:minSdkVersion="26"
tools:overrideLibrary="com.bluejeans.bluejeanssdk"/>
```

### Install BlueJeans Android Client SDK:

We distribute our SDK from the Maven Repository.

To add the SDK to your app, add the following dependency in your build.gradle files:

In top level project build.gradle
```xml
repositories { maven { url "https://swdl.bluejeans.com/bjnvideosdk/android" } }
```

In app's build.gradle
```xml
implementation "com.bluejeans:android-client-sdk:1.0.0"
```

### Upgrade Instructions:
Whenever a newer version of SDK is available, you can consume it by increasing the version in the implementation code block to the new SDK version.


## Initialize BlueJeans SDK:
Create the object of BlueJeans SDK in application onCreate with help of application context and use it to access all the APIs

Minimum permission needed to join a meeting is permission for RECORD_AUDIO. Make sure app requests this permission before calling Join API.

Sample Code:
```java
blueJeansSDK = new BlueJeansSDK(new BlueJeansSDKInitParams(this));
```

APIs are grouped into relevant services as in the architecture diagram. All the service objects are available all the time after SDK instantiation, however all are not active all the time.
When inactive, APIs of the services do not react and the subscriptions will yield null. 

**List of services :** 

**_Globally active services_** -> MeetingService, VideoDeviceService, LoggingService and PermissionService.

**_InMeeting active services_** -> ContentShareService, AudioDeviceService, PublicChatService, PrivateChatService and ParticipantsService

InMeeting services get activated when _MeetingState_ transitions from _MeetingState.Validating_ to _MeetingState.Connecting_ and get inactivated
when meeting ends by the transition of meeting state to _MeetingState.Disconnected_

PermissionService : Provides for permission handling related APIs (refer to documentation for API set and details)

```java
blueJeansSDK.getPermissionService
```
LoggingService : Provides for SDK logging related APIs (refer to documentation for API set and details)

```java
blueJeansSDK.getLoggingService
```

VideoDeviceService : Provides for video device enumeration and self video preview enablement APIs (refer to documentation for API set and details)

```java
blueJeansSDK.getVideoDeviceService
```

MeetingService : Provides for meeting related APIs and all inMeeting Services

```java
blueJeansSDK.getMeetingService
```

## Join a BlueJeans meeting:
It is recommended to start a foreground service before getting into the meeting. If you are not familiar with [foreground services](https://developer.android.com/guide/components/foreground-services) and [notfications](https://developer.android.com/reference/android/app/Notification), we suggest you to learn about these before proceeding with this section.

Starting a foreground service ensures we have all the system resources available to our app even when in background, thereby not compromising on audio quality, content capture quality during features like content share and also prevents app from being killed due to lack of resources in background.

Refer *OnGoingMeetingService* and *MeetingNotificationUtility* for sample implementation.

**Note:**
- foreground service is not needed if your app runs on a platform where it will never be put to background.

### Steps to join meeting
- Provide Mic(RecordAudio) and Camera Permissions either by using BJN SDK permissionService or by Android SDK APIs
- Get and add *SelfVideoFragment* and *enableSelfVideoPreview* to start the self video
- Get and use meeting service and invoke join APIs to join a meeting
- Observe for Join API result by subscribing to the Rx Single returned by the join API


#### VideoDeviceService (Video device enumeration, Selection):

*enableSelfVideoPreview* provides for enabling/disabling the camera capturer. Functional irrespective of meeting state.

*videoDevices* will provide a list of video/camera devices available on the hardware.

*currentVideoDevice* will provide for the current device selected. By default the front camera is selected.

Use *selectVideoDevice* and choose the video device of your choice from the available *videoDevices* list.

##### Setting capture request:

BlueJeans SDK provides for capability to *setRepeatingCaptureRequest* which internally deligates the request to Camera2's [setRepeatingRequest](https://developer.android.com/reference/android/hardware/camera2/CameraCaptureSession#setRepeatingRequest(android.hardware.camera2.CaptureRequest,%20android.hardware.camera2.CameraCaptureSession.CaptureCallback,%20android.os.Handler)) on the currently running camera session. This opens up options to set any [CaptureRequest](https://developer.android.com/reference/android/hardware/camera2/CaptureRequest) of integrator's choice. Most commonly used option could be to set Zoom, the same is show cased in the sample test app.

##### Setting torch mode:

BlueJeans SDK provides for capability to  turn ON/OFF torch. *setTorchMode* API sets the flash unit's torch mode of the video device for the given ID. Note : Some of the devices need the torch supported camera device to be open for the torch to be turned ON.

## Meeting Service:
This service takes care of all meeting related APIs. Apart from meeting related APIs, the service also provides provides for several inMeeting services - ParticipantsService, AudioDeviceService, ContentShareService, PublicChatService and PrivateChatService.

### Video Layouts:

Represents how remote participants videos are composed
- **Speaker**: Only the most recent speaker is shown, taking up the whole video stream.
- **People**: The most recent speaker is shown as a larger video. A filmstrip of the next (up to) 5 most recent speakers is shown at the top.
- **Gallery**: Bunch of most recent speakers are shown, arranged in a grid layout of equal sizes.

*videoLayout* provides for the current video layout *setVideoLayout* can be used to force a Video Layout of your choice.

Note that by default the current layout will be the People layout or it will be the one chosen by the meeting scheduler in his accounts meeting settings.


#### Different layouts, number of tiles:
- `Speaker layout` to fit one single active speaker participant
- `People layout` to fit max 6 participants, 1 (main active speaker participant) + 5 (film strip participants)
- `Gallery layout` can fit maximum number of participant tiles as 9 or 25 depending on SDK input configuration. By default it is 9 participants, ordered in 3x3 style. This is configurable to support max of 25 participants, ordered in 5x5 style

#### Configuring 5x5 in gallery layout:
BlueJeansSDKInitParams provides a new input parameter called videoConfiguration which can be set with value GalleryLayoutConfiguration.FiveByFive. It is recommended to set this only on larger form factor (>= 7") devices for a better visual experience. Note that using 5x5 will consume higher memory, CPU and battery as compared to other layouts

### Remote Video:

The BlueJeans SDK's RemoteVideoFragment provides for both the audio and video participant tiles. The organization and the ordering of these tiles depend on factors namely recent dominant speaker and meeting layout, in addition to an algorithm that ensures minimal movement of tiles when recent speaker changes. Video participants are given the priority and are put first in the list and then the audio participants follow.

Note: MultiStream mode is not supported on devices with number of CPU cores less than six. In such cases, RemoteVideoFragment would receive single composited stream (participants videos are stitched at the server, organized based on the layout chosen and a single stream is served to the client).

### Video Resolutions and BW consumption:

- Video receive resolution and BW max:

| Layout       | Max participants| Layout pattern for max participants| Video Receive max resolution, FPS             | Video Receive BW max |
| -------------|:---------------:| :---------------------------------:| :--------------------------------------------:|:--------------------:|
| Speaker View | 1               | 1                                  | 640x360/640x480  30fps                        | 600 kbps             |
| People View  | 6               | 1 (main stage) + 5 (film strip)    | main stage 640x360/640x480 30fps              | 1100 kbps            |
|              |                 |                                    | film strip 160x90/120x90, 15fps               |                      |
| Gallery View | 9               | 3x3 (landscape) / 4x2+1 (portrait) | 640x360/640x480 (participants < 2)      30 fps| 1200 kbps            |
|              |                 |                                    | 320x180/240x180 (participants > 2, < 4) 30 fps| 1200 kbps            |
|              |                 |                                    | 160x90/120x90  (participants > 4)       15 fps| 900 kbps             |
|              |                 |                                    | 160x90/120x90  (participants > 9)       15 fps| 1700 kbps            |

- Content receive resolution and BW max: 1920x1080 at 5 fps, 300 kbps
- Video send resolution and BW max: 640x480 at 30fps, 900 kbps

Note: Endpoints which send video in aspect ratio of 4:3 instead of 16:9, will result in video receive resolution of 640x480 in place of 640x360, 240x180 in place of 320x180 and 120x90 in place of 160x90. Mobile endpoints / BlueJeans android SDK endpoints send video at 640x480 i.e at aspect ratio of 4:3.

### Mute:

The BluejeansSDK provides APIs to mute/unmute self video.

`enableSelfVideoPreview` of VideoDeviceService controls video device enablement. This drives the self video preview.
`setVideoMuted` of MeetingService will mute/unmute the self video stream flowing to the other endpoint. This API additionally triggers self video preview states internally.
 Available when meeting state moves to MeetingState.Connected

Note that
- video mute state applied on `enableSelfVideoPreview` needs to be applied to `setVideoMuted` once meeting state changes to MeetingState.Connected.
- when in a meeting (meeting state is MeetingState.Connected) and if `setVideoMuted` is called with true, `enableSelfVideoPreview` is called with true,
then the self video preview gets activated but the stream does not flow to the other endpoint.

#### Mute/Unmute Remote Video:
The BluejeansSDK MeetingService provides API to mute, unmute remote participants video. This is helpful in the scenarios where the user does not intend to view remote video.
Some example use cases can be
- App has a viewpager with first page showing remote video and second page showing content. When user is on content page, this API can be used to mute remote video.
- To provide audio only mode
- App going in background
**Note:** This API does not give instant result, this may take upto5 sec in case of back to back requests.

##### API:
`meetingService.setRemoteVideoMuted(muted: Boolean)`

#### Mute/Unmute Content:
The BluejeansSDK MeetingService provides API to mute, unmute content. This is helpful in the scenarios where the user does not intend to view content.
Some example use cases can be
- App has a viewpager with first page showing remote video and second page showing content. When user is on video page, this API can be used to mute content.
- To provide audio only mode
- App receiving content and goes in background
**Note:** This API does not give instant result, this may take upto 5sec in case of back to back requests. Unlike for video, we have a single API to mute content share and mute content receive. Ensure to call this only when you are not sharing the content from your end.

##### API:
`meetingService.setContentMuted(muted: Boolean)`

#### Background handling recommendations:
When the app is put to background and user is out of meeting: User's self video needs to be stopped to save CPU load, save battery
When the app is put to background and user is in a meeting:
- User's self video needs to be stopped for privacy reasons
- Remote video and content receive should be muted to save bandwidth

and the same can be turned ON when the app is put back to foreground.

These can be achieved using the set of mute APIs SDK provides for.
Use `setVideoMuted` for managing self video flowing to other endpoints when in a meeting
Use `enableSelfVideoPreview` for managing the capturer when not in meeting
Use `setRemoteVideoMuted` for managing remote participants video when in a meeting
Use `setContentMuted` for managing content when in a meeting

## Audio Device Service (Audio device enumeration, Selection):

```java
blueJeansSDK.getMeetingService().getAudioDeviceService()
```

*audioDevices* will provide a list of audio devices available on the hardware.

*currentAudioDevice* will provide for the current audio device selected.

On dynamic change in audio devices, SDK's default order of auto selection is as below:

- BluetoothHeadset
- USBDevice
- USBHeadset
- WiredHeadsetDevice
- WiredHeadPhones
- Speaker

Use *selectAudioDevice* and choose the audio device of your choice from the available *audioDevices* list.

## Participants Service:

```java
blueJeansSDK.getMeetingService().getParticipantsService()
```

*participant* represents a meeting participant. Carries properties video mute state, audio mute state, is self, name and an unique identifier of the participant.

*participants* provides for list of meeting participants. The list will be published on any change in the number of meeting participants or the change in properties of any of the current participants. Any change will reflect in the content of the list and the list reference remains same all throughout the meeting instance.

*selfParticipant* represents self. Provides for any changes in properties of the self.

### Content Share Feature:

Provides facility to share content within the meeting, there by enhances the collaborative experience of the meeting session. Our SDK currently supports content share in the form of full device's screen share which can be access via ContentShareService
```java
blueJeansSDK.getMeetingService().getContentShareService()
```

#### Sharing the Screen :
This is a full device screen share, where the SDK will have access to all of the information that is visible on the screen or played from your device while recording or casting. This includes information such as passwords, payment details, photos, messages, and audio that you play. This information about data captured is prompted to the user as a part of starting screen share permission consent dialog and the data populated within the dialog comes from android framework.

##### Feature pre requisites :
- Permission to capture screen data using android's Media Projection Manager
- Foreground service

##### Sample code to ask permission :

      MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
      startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREEN_SHARE_REQUEST_CODE);

The above request will result in a permission dialog like

<img width="320" alt="ScreenSharePermissionDialog" src="https://user-images.githubusercontent.com/23289872/114588998-e2e28e00-9ca4-11eb-850c-7b0396a068fd.png">

Note that, this is a system prompted dialog where the name in the dialog will be the app name and the content is a predefined by the Android System.

##### Start foreground service, Screen Share :
Once you get the result of the permission, start a service and then invoke _startContentShare_ API :

**Note:** If you already have a foreground service running for the meeting, then an explicit service for screen sharing is not needed.
Apps targeting SDK version `Build.VERSION_CODES.Q` or later should specify the foreground service type using the attribute `R.attr.foregroundServiceType`
in the service element of the app's manifest file as below

      <service
         android:name=".YourForegroundService"
         android:stopWithTask="true"
         android:foregroundServiceType="mediaProjection"/>

##### Invoke content share start API :

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SCREEN_SHARE_REQUEST_CODE:
                if (data != null) {
                     startYourForegroundService() // start your service if there is not one already for meeting
                     contentShareService.startContentShare(new ContentShareType.Screen(data));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

### Start Share API :
`startContentShare(contentShareType: ContentShareType)`

### Stop Share API :
`stopContentShare()`

#### Observables with Screen Share feature :
- contentShareState - provides for screen share current state
- contentShareEvent - provides for screen share events
- contentShareAvailability - provides for information about content share feature being available or not. Content share may not be available in cases such as an access disabled by moderator, access disabled by admin, due to lack of meeting privilege and enablement of moderator only share.

#### User experience recommendation :
Whenever Screen Share starts,
- Put the app to background there by easing the user to chose screen/app of his choice
- Have a overlay floater button out of app, which can be used to stopping the screen share
- Stopping screen share using floater button can bring the app back to foreground
- Put an overlay border around the screen to indicate that the screen share in progress

## Meeting Chat Feature:
The Bluejeans SDK's MeetingService provides facility to chat within the meeting with participants present in the meeting. The chat will remain during the
duration of the meeting and will be cleared once the meeting is over.

There are two types of chat services available

## Public Chat Service :
The service provides APIs to message all participants present in meeting at once i.e. All the participants present in the meeting, will receive the message sent through the service.

**Note:** Whenever a new user joins or reconnection happens only last 10 public messages are restored.

#### API :
`meetingService.publicChatService.sendMessage(message: String): Boolean` </br>

## Private Chat Service :
The service provides APIs to message individual participants present in the meeting i.e. Only the participant given as input to API will get the message. All participants present in meeting may or maynot
be eligible for private chat. The service provides a list of eligible participants and only those participants will be available for private chat.

**Note:** Whenever a participant disconnects and connects back in same meeting, it is treated as a new user and previous chat messages if any will not be retained.

#### API :
`meetingService.privateChatService.sendMessage(message: String, participant: Participant): Boolean`

There Rx Subscriptions are provided by each of chat services to listen to message and unread message count. Please refer to API documentation for more details.

## Logging Service :
Provides for uploading logs to BJN sdk log server. The API takes user comments and the user name.
Name of the user serves as an unique identifier for us to identify the logs uploaded.

#### API :
`uploadLog(comments: String, username: String)`

#### Single Result :
AlreadyUploading, Success, Failed

## Subscriptions (ObservableValue and Rx Single's):

#### RxSingle: 
This is a standard [Rx Single](http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html)

#### ObservableValue:

Most of our subscriptions are stateful members called ObservableValues. 
These are our BJN custom reactive stream elements carrying a value that can be accessed (READ only) at any point of time and also allows a subscription. Through ObservableValue you can also access [RxObservable](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) and subscribe.

Sample app depicts usage of both the RxSingle and ObeservableValue


## SDK Sample Application:
We have bundled two sample apps in this repo. One for Java and another for kotlin.
It show cases the integration of BlueJeans SDK for permission flow and join flow. They have got a basic UI functionality and orientation support.

## Tracking & Analytics:
BlueJeans collects data from app clients who integrates with SDK to join BlueJeans meetings like Device information (ID, OS etc.), Location and usage data.

## Contributing:
The BlueJeans Android Client SDK is closed source and proprietary. As a result, we cannot accept pull requests. However, we enthusiastically welcome feedback on how to make our SDK better. If you think you have found a bug, or have an improvement or feature request, please file a GitHub issue and we will get back to you. Thanks in advance for your help!

## License: 
Copyright © 2021 BlueJeans Network. All usage of the SDK is subject to the Developer Agreement that can be found [here](LICENSE). Download the agreement and send an email to api-sdk@bluejeans.com with a signed version of this agreement, before any commercial or public facing usage of this SDK.

## Legal Requirements:
Use of this SDK is subject to our [Terms & Conditions](https://www.bluejeans.com/terms-and-conditions-may-2020) and [Privacy Policy](https://www.bluejeans.com/privacy-policy). 
