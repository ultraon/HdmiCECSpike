# HdmiCECSpike
Investigation on HDMI-CEC technology for CTV (leanback)

GoogleTV observations:
	
	CTV & GoogleTV HDMI-CEC is enabled:

	!- when I turn off the TV (even when TV Input Source is another) - Activity.onStop (the app is minimized), the Activity is still alive (but stopped)

	!- when I switch TV Input Source from GoogleTV HDMI - no any message

	- when I launch a spike app:
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Added device (1): name: Chromecast, type: 21 - TYPE_BUS, sink: true, source: false
			Added device (2): name: Chromecast, type: 15 - TYPE_BUILTIN_MIC, sink: false, source: true
			Added device (3): name: Chromecast, type: 15 - TYPE_BUILTIN_MIC, sink: false, source: true
			Broadcast, extraAudioPlugState: 1 - PLUGGED

	- when I switch TV Input Source to GoogleTV HDMI:
			Removed device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 1 - PLUGGED

	- when I disconnected HDMI:
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Removed device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false

	- when I connected HDMI back again:
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 1 - PLUGGED
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Removed device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 1 - PLUGGED


	CTV HDMI-CEC is disabled, GoogleTV HDMI-CEC is enabled or disabled:

	!- when I turn off the TV - no any signal or message

	!- when I switch TV Input Source from GoogleTV HDMI - no any message

	- when I turn on the TV:
			Removed device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 1 - PLUGGED

	- when I disconnected HDMI:
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Removed device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false

	- when I connected HDMI back again:
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 1 - PLUGGED

	- when I switch TV Input Source to GoogleTV HDMI:
			Removed device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Added device (0): name: Chromecast, type: 9 - TYPE_HDMI, sink: true, source: false
			Broadcast, extraAudioPlugState: 1 - PLUGGED




FireTV 4K Max observations:

	CTV & FireTV HDMI-CEC is enabled (the only difference with GoogleTV):

	!- when I switch TV Input Source from FireTV HDMI (only when another HDMI Input was selected, not analogue or digital TV):
			Broadcast, extraAudioPlugState: 0 - UNPLUGGED
			Removed device (0): name: AFTKA, type: 9 - TYPE_HDMI, sink: true, source: false



**Summary:**

 CTV & GoogleTV HDMI-CEC is enabled:
	- need a 5 sec throttle while listening for the HDMI events
	- there is no events when another TV Input Source is switched to
	- in fact we need only listen to for RemovedDevice or UNPLUGGED events

 CTV HDMI-CEC is disabled, GoogleTV HDMI-CEC is enabled (or disabled):
 	- we still can get notifications when HDMI is disconnected
 	- we still can get notifications when GoogleTV Input Source is activated

 - For Android 6 (API 23) and above we need to use `audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)`
 - For android 5 (API 21-22) we need to use BroadcastReceiver with `IntentFilter().apply { addAction(ACTION_HDMI_AUDIO_PLUG) }`



**Conclusion:**

 - (POWER OFF - HDMI-CEC) The main difference when HDMI-CEC is enabled (on both CTV + Stick) from disabled is reaction to power-off button, the App Activity goes in onStop (CREATED state), so the player is destroyed in any case
 - (CHANGE INPUT - only FireTV) We can detect when the TV Input Source is changed to another one only for FireTV and only if another HDMI device was selected (not a analogue or digital TV), and it is not HDMI-CEC feature
 - (DISCONNECT HDMI) Even without HDMI-CEC technology the app is still available to listen for events when HDMI is disconnected and check the HDMI connection state (connected/disconnected)
 - (CONNECT HDMI) Even without HDMI-CEC technology the app is still available to listen for events when HDMI is connected and check the HDMI connection state (connected/disconnected)
 - (ACTIVATED INPUT) We can determine if the INPUT SOURCE was activated again
 
