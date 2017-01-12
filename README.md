# Titanium Module for Google Cloud Messaging Push Notifications for Android #

A Titanium module for registering a device with Google Cloud Messaging and handling push notifications sent to the device.

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

Read the [documentation](https://github.com/morinel/gcmpush/blob/master/documentation/index.md).

## Implementing in your app

### Installing

```shell
$ gittio install nl.vanvianen.android.gcm
```

### Update your tiapp.xml

Get your GCM sender ID from the Google GCM control panel, then add it to your tiapp.xml:

```xml
<property name="GCM_sender_id" type="string">123456789</property>
```

Gittio should have added the module tag, like this:

```xml
<modules>
  <module platform="android">nl.vanvianen.android.gcm</module>
</modules>
```

The module requires various permissions, which should be merged into your app's manifest during the build process from the timodule.xml file in this repo. You might need to add those tags to your tiapp.xml. If you do, make sure to update them with your app's ID.

### In your app, register for & handle push notifications

In general terms, this is how the module will work for you:

* You'll register to receive push messages in your app's root controller (that corresponds to your app's root Activity). Generally, that will be index.js for an Alloy app (not alloy.js!) or app.js for a Classic app.
* You'll specify handler functions that will run when:
	* you successfully register for handling push messages
	* or, if there's an error registering
	* and when your app receives a push message
* Finally, your app will need to handle the push message when resuming from the background
 
Starting from the top, in your root controller, register to receive push messages:

```
// instantiate the module
var gcm = OS_ANDROID ? require("nl.vanvianen.android.gcm") : undefined,
	GCM_SENDER_ID = '123456789'; // get from the Google GCM admin console

// register for pushes, see the docs in this repo for more info on the options
gcm.registerPush({
	senderId: GCM_SENDER_ID,
	notificationSettings: {
		sound: 'optionalCustomSound.wav',
		/* Place sound file in platform/android/res/raw/ */
		smallIcon: 'notification_icon.png',
		/* Place icon in platform/android/res/drawable/notification_icon.png */
		largeIcon: 'appicon.png',
		/* Same place */
		vibrate: true,
		/* Whether the phone should vibrate */
		insistent: false,
		/* Whether the notification should be insistent */
		group: 'OptionalAppGroupName',
		/* If specified, mult. pushes will be shown in a single entry in the tray */
		localOnly: false,
		/* Whether this notification should be bridged to other devices */
		backgroundOnly: true,
		priority: +2 /* Notification priority, see the docs */
	},
	// register your push-related event handlers:
	success: deviceTokenSuccess,
	error: deviceTokenError,
	callback: pushReceived
});
```

Next, you must specify what you're app will do in response to the various push-related events:

```

function deviceTokenSuccess(e) {
	// called when the app successfully registers to receive push notifications
	// for example, record the device's push token
	console.log("The device's push token is " + e.registrationId);
}

function deviceTokenError(e) {
	// called if there's an error while registering for pushes
	console.log("Error during registration: " + e.error);
}

function pushReceived (e) {
		// called when a push is received, but this isn't generally where you
		// handle pushes because this will be called whether the app is active
		// or in the background
	}
```


Really, you want to handle the push messages (and their payload) in your root controller's resume listener. But, this can be a bit tricky. 

Unless you have a single window (Activity) app, the root activity is going to get paused every time you open a new window. You don't want your push payload handler running each time that new window is closed and your root window "resumes." 

Instead, you're going to want to track whether your app is really backgrounded so that you can handle the resumes intelligently. For example, you might set `Alloy.Globals.AppIsActive = true` in each of your windows and have your root controller's pause handler set it to `false`. Then, check that global in your root controller's resume handler to know when to process push-related data.

```
function appresume() { 
	if (OS_ANDROID && !Alloy.Globals.AppIsActive) {
		var gcmData = gcm.getLastData();
		// do whatever with the gcmData (the push message payload)
		gcm.clearLastData(); // then clear it when you're done
	}
	Alloy.Globals.AppIsActive = true;
}

function apppause() {
	Alloy.Globals.AppIsActive = false;
}
```

Of course, you need to register those lifecycle listeners on your root controller's activity:

```
// finally, also in index.js
var activity = $.index.activity;
activity.onResume = appresume;
activity.onPause = apppause;

// see the docs for other activity events, like:
activity.onDestroy = appdestroy; // clean up stuff when app is fully closed
activity.onCreate = appcreate; // stuff to do when app is cold started
```

**Note:** As of this writing, the app has no way of knowing whether it was launched by the user tapping the app's icon on the home screen or by tapping a push notification in the tray. This is a Titanium limitation, which was supposedly at least partially addressed in Titanium 6.0.0.GA. This module will need to be updated to support the changes. Further testing is needed to confirm that Titanium was properly updated to support launchIntents.

## Building from source

To build, create a `build.properties` file with the following content:

```
titanium.platform=/Users/${user.name}/Library/Application Support/Titanium/mobilesdk/osx/6.0.0.GA/android
android.platform=/Users/${user.name}/Library/Android/sdk/platforms/android-23
google.apis=/Users/${user.name}/Library/Android/sdk/add-ons/addon-google_apis-google-23
android.ndk=/Users/${user.name}/Library/Android/ndk
```

Of course, make sure the paths are correct for your system. 

Then run:

```
$ ant clean
$ ant
```

A zip file will be created in the `dist` folder.
