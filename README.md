[![BlueJeans Android Software Development Kit](https://raw.githubusercontent.com/bluejeans/sdk-webrtc-meetings/master/media/BlueJeans_Mark.png "BlueJeans Android Software Development Kit")](https://www.bluejeans.com "BlueJeans Android Software Development Kit")
# BlueJeans Android Software Development Kit

The BlueJeans Android Software Development Kit (SDK) gives a quick and easy way to bring immersive video-calling experience into your android applications.
Note that the product is currently in **alpha** phase of its release cycle and is under active development.

### Features:
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

### New Features:
- Screen Share
- Log Upload

### Current Version: 1.0.0-alpha.3

### Pre-requisites:
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

### Documentation: [Dokka docs](https://bluejeans.github.io/android-sdk)

### Integration Steps:
#### Override Minimum SDK Version:
This version of BlueJeans Android SDK is compatible with Android version "26" or higher. Therefore, your Android app must have a minimum SDK version 26 or higher.  
 
If your app runs with min SDK version below API level 26, you must override min SDK version as in the below sample. However please note that, SDK instantiation will fail if the app runs on API level below 26, please add build check to avoid SDK instantiation on device with API level < 26. 
  
**Sample Code:** Add the below to your AndroidManifest.xml  
```xml
<uses-sdk android:minSdkVersion="26"
tools:overrideLibrary="com.bluejeans.bluejeanssdk"/>
```

#### Install BlueJeans Android SDK:

We distribute our SDK from the Maven Repository.

To add the SDK to your app, add the following dependency in your build.gradle files:

In top level project build.gradle
```xml
repositories { maven { url "https://swdl.bluejeans.com/bjnvideosdk/android" } }
```

In app's build.gradle
```xml
implementation "com.bluejeans:sdk-android:1.0.0-alpha.3"
```

#### Upgrade Instructions:
Whenever a newer version of SDK is available, you can consume it by increasing the version in the implementation code block to the new SDK version.

### Initialize BlueJeans SDK:
Create the object of BlueJeans SDK in application onCreate with help of application context and use it to access all the APIs

Minimum permission needed to join a meeting is permission for RECORD_AUDIO. Make sure app requests this permission before calling Join API.

Sample Code:
```java
blueJeansSDK = new BlueJeansSDK(new BlueJeansSDKInitParams(this));
```
APIs are exposed through two major services, MeetingService and PermissionService. These objects can be accessed in code as 

```java
blueJeansSDK.getMeetingService 
```
This takes care of all meeting related APIs (refer to documentation for API set and details)

```java
blueJeansSDK.getPermissionService
```

This takes care of permission handling related APIs (refer to documentation for API set and details)

#### Join a BlueJeans meeting:
It is recommended to start a foreground service before getting into the meeting. If you are not familiar with [foreground services](https://developer.android.com/guide/components/foreground-services) and [notfications](https://developer.android.com/reference/android/app/Notification), we suggest you to learn about these before proceeding with this section.

Starting a foreground service ensures we have all the system resources available to our app even when in background, thereby not compromising on audio quality, content capture quality during features like content share and also prevents app from being killed due to lack of resources in background.

Refer OnGoingMeetingService and MeetingNotificationUtility for sample implementation.

**Note:** 
- foreground service is not needed if your app runs on a platform where it will never be put to background.

#### Steps to join meeting
- Provide Mic(RecordAudio) and Camera Permissions either by using BJN SDK permissionService or by Android SDK APIs
- Get and add SelfVideoFragment and enableSelfVideoPreview to start the self video
- Get and use meeting service and invoke join APIs to join a meeting
- Observe for Join API result by subscribing to the Rx Single returned by the join API



#### Managing Self Video:


##### Orientation handling:

If Meeting activity handles
**onConfigurationChanged:** Call *setSelfVideoOrientation*

If Meeting activity recreates on orientation change:
No need to call any additional API. 


##### Mute:

*enableSelfVideoPreview* provides for enabling/disabling the camera capturer. Functional irrespective of meeting state.
 
*setVideoMuted* will mute the video stream flowing to the other endpoint. Functional when in a meeting.

Integrated app should use these APIs in combination to drive the selfvideo mute.


#### Video device enumeration, Selection:

*videoDevices* will provide a list of video/camera devices available on the hardware.

*currentVideoDevice* will provide for the current device selected. By default the front camera is selected.

Use *selectVideoDevice* and choose the video device of your choice from the available *videoDevices* list.


#### Audio device enumeration, Selection: 
(Available only after joining a meeting)

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

#### Video Layouts:

Represents how remote participants videos are composed
- **Speaker**: Only the most recent speaker is shown, taking up the whole video stream.
- **People**: The most recent speaker is shown as a larger video. A filmstrip of the next (up to) 5 most recent speakers is shown at the top.
- **Gallery**: Bunch of most recent speakers are shown, arranged in a grid layout of equal sizes.

*videoLayout* provides for the current video layout *setVideoLayout* can be used to force a Video Layout of your choice.

Note that by default the current layout will be the People layout or it will be the one chosen by the meeting scheduler in his accounts meeting settings. 

#### Participant List:

*participant* represents a meeting participant. Carries properties video mute state, audio mute state, is self, name and an unique identifier of the participant.

*participants* provides for list of meeting participants. The list will be published on any change in the number of meeting participants or the change in properties of any of the current participants. Any change will reflect in the content of the list and the list reference remains same all throughout the meeting instance.

*selfParticipant* represents self. Provides for any changes in properties of the self.

#### Content Share Feature :

Provides facility to share content within the meeting, there by enhances the collaborative experience of the meeting session. Our SDK currently supports content share in the form of full device's screen share.

##### Sharing the Screen :
This is a full device screen share, where the SDK will have access to all of the information that is visible on the screen or played from your device while recording or casting. This includes information such as passwords, payment details, photos, messages, and audio that you play. This information about data captured is prompted to the user as a part of starting screen share permission consent dialog and the data populated within the dialog comes from android framework.

###### Feature pre requisites :
- Permission to capture screen data using android's Media Projection Manager
- Foreground service

###### Sample code to ask permission :

      MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
      startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREEN_SHARE_REQUEST_CODE);

The above request will result in a permission dialog like 

<img width="320" alt="ScreenSharePermissionDialog" src="https://user-images.githubusercontent.com/23289872/114588998-e2e28e00-9ca4-11eb-850c-7b0396a068fd.png">

Note that, this is a system prompted dialog where the name in the dialog will be the app name and the content is a predefined by the Android System.

###### Start foreground service, Screen Share :
Once you get the result of the permission, start a service and then invoke _startContentShare_ API :

**Note:** If you already have a foreground service running for the meeting, then an explicit service for screen sharing is not needed.
Apps targeting SDK version `Build.VERSION_CODES.Q` or later should specify the foreground service type using the attribute `R.attr.foregroundServiceType`
in the service element of the app's manifest file as below

      <service
         android:name=".YourForegroundService"
         android:stopWithTask="true"
         android:foregroundServiceType="mediaProjection"/>

###### Invoke content share start API :
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SCREEN_SHARE_REQUEST_CODE:
                if (data != null) {
                     startYourForegroundService() // start your service if there is not one already for meeting
                     mMeetingService.startContentShare(new ContentShareType.Screen(data));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
###### Start Share API :
`startContentShare(contentShareType: ContentShareType)`

###### Stop Share API : 
`stopContentShare()`

###### Observables with Screen Share feature :
- contentShareState - provides for screen share current state
- contentShareEvent - provides for screen share events
- contentShareAvailability - provides for information about content share feature being available or not. Content share may not be available in cases such as an access disabled by moderator, access disabled by admin, due to lack of meeting privilege and enablement of moderator only share.

###### User experience recommendation :
Whenever Screen Share starts,
- Put the app to background there by easing the user to chose screen/app of his choice
- Have a overlay floater button out of app, which can be used to stopping the screen share
- Stopping screen share using floater button can bring the app back to foreground
- Put an overlay border around the screen to indicate that the screen share in progress

#### Log Upload Feature :
Provides for uploading logs to BJN sdk log server. The API takes user comments and the user name. 
Name of the user serves as an unique identifier for us to identify the logs uploaded.

##### API : 
`uploadLog(comments: String, username: String)`

##### Single Result : 
AlreadyUploading, Success, Failed

#### Subscriptions (ObservableValue and Rx Single's):

##### RxSingle: 
This is a standard [Rx Single](http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html)

##### ObservableValue:

Most of our subscriptions are stateful members called ObservableValues. 
These are our BJN custom reactive stream elements carrying a value that can be accessed (READ only) at any point of time and also allows a subscription. Through ObservableValue you can also access [RxObservable](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) and subscribe.

Sample app depicts usage of both the RxSingle and ObeservableValue


### SDK Sample Application:
Sample app provided in this repo is a java sample app.
It show cases the integration of BlueJeans SDK for permission flow and join flow. They have got a basic UI functionality and orientation support.

### Contributing:
The BlueJeans Android SDK is closed source and proprietary. As a result, we cannot accept pull requests. However, we enthusiastically welcome feedback on how to make our SDK better. If you think you have found a bug, or have an improvement or feature request, please file a GitHub issue and we will get back to you. Thanks in advance for your help!

### License:
Copyright © 2021 BlueJeans Network. All usage of the SDK is subject to the Developer Agreement that can be found [here](LICENSE). You are expected to email api-sdk@bluejeans.com with a signed version of this agreement before any commercial or public facing usage of this SDK.
