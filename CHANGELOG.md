Change Log
==========

Version 1.0.0-alpha.1 *(02-2021)*
---------------------------------
Features:
- Audio and Video Permission handling. 
- Join, End Meeting. 
- Self Video. 
- Remote Video, Remote Video states. 
- Content receive. 
- Audio and Video self mute. 
- Orientation handling. 
- Video device enumeration, Selection. 
- Audio device enumeration, Selection. 
- Video Layout switch.

Version 1.0.0-alpha.2 *(03-2021)*
---------------------------------
New Features:
- Meeting participants list.
- Participant properties: Audio mute state, Video mute state, isSelf, Unique identifier and Name.
- Self Participant details.

Version 1.0.0-alpha.3 *(04-2021)*
---------------------------------
New Features:
- Content Share.
- Log Upload.


Version 1.0.0-alpha.4 *(05-2021)*
---------------------------------
Changes :

- Multi stream support (Sequin video layouts). We would receive individual remote streams in place of single composited video from server providing a better video experience with enhanced meeting layouts.
- RxJava upgraded from version 2.0 to version 3.0. If consumer app is already using RxJava2 then additionally RxJava3 should be added to consume BlueJeansSDK reactive properties.
- Removed setSelfVideoOrientation. SelfVideoFragment can now handle orientation changes on its own.
- BlueJeansSDKInitParams carries a new configuration parameter by name videoConfiguration which allows you to configure number of maximum participants in the Gallery Video Layout
- Fix for webRTC class conflict issue. Consumer app can now bundle webRTC libraries in addition to BlueJeans SDK
