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
```xml
repositories {
        maven {
            url publishURL
            credentials(AwsCredentials) {
                accessKey AWSAccessKey
                secretKey AWSSecretKey
            }
        }
    }
```
```xml
implementation("com.bluejeans:sdk-android:1.0.0-alpha.1'") {
        transitive(true)
    }
```
URL and keys will be shared separately.

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

Represents how remote participantâ€™s videos are composed
- **Speaker**: Only the most recent speaker is shown, taking up the whole video stream.
- **People**: The most recent speaker is shown as a larger video. A filmstrip of the next (up to) 5 most recent speakers is shown at the top.
- **Gallery**: Bunch of most recent speakers are shown, arranged in a grid layout of equal sizes.

*videoLayout* provides for the current video layout *setVideoLayout* can be used to force a Video Layout of your choice.

Note that by default the current layout will be the People layout or it will be the one chosen by the meeting scheduler in his accountâ€™s meeting settings. 

#### Subscriptions (ObservableValue and Rx Single's):

##### Single's:

http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html

##### ObservableValues:

Most of our subscriptions are stateful members called ObservableValues. 

These are our BJN custom reactive stream elements carrying a value that can be accessed (READ only) at any point of time and also allows a subscription.

Through ObservableValue you can also access rxObservable(http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) and subscribe.

Sample app depicts usage of both the RxSingle and ObeservableValue


### SDK Sample Application:
Sample app provided in this repo is a java sample app.
It show cases the integration of BlueJeans SDK for permission flow and join flow. They have got a basic UI functionality and orientation support.

### Contributing:
The BlueJeans Android SDK is closed source and proprietary. As a result, we cannot accept pull requests. However, we enthusiastically welcome feedback on how to make our SDK better. If you think you have found a bug, or have an improvement or feature request, please file a GitHub issue and we will get back to you. Thanks in advance for your help!

### License:
Copyright © 2020 BlueJeans Network. All rights reserved.
