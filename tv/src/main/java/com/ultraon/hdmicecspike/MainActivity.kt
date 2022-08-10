package com.ultraon.hdmicecspike

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioManager.ACTION_HDMI_AUDIO_PLUG
import android.media.AudioManager.EXTRA_AUDIO_PLUG_STATE
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity


/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    private val audioManager by lazy { getSystemService<AudioManager>()!! }

    private val audioDeviceCallback by lazy { AudioDeviceCallbackImpl() }

    private val hdmiEventReceiver = HdmiEventReceiver()

    private var disposable: IDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.test_text).apply {
            text = "See LogCat output"
        }
    }

    override fun onStart() {
        super.onStart()
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        disposable?.dispose()
        disposable = hdmiEventReceiver.attach(this) { extraAudioPlugState ->
            Log.i(TAG, "Broadcast, extraAudioPlugState: $extraAudioPlugState")
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop")
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        disposable?.dispose()
        disposable = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
    }

    class AudioDeviceCallbackImpl : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesAdded(addedDevices)
            addedDevices?.takeIf { it.isNotEmpty() }
                ?.forEachIndexed { i, device ->
                    Log.i(TAG, "Added device ($i): ${device.toStringDebug()}")
                } ?: run { Log.w(TAG, "addedDevices are empty") }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesRemoved(removedDevices)
            removedDevices?.takeIf { it.isNotEmpty() }
                ?.forEachIndexed { i, device ->
                    Log.i(TAG, "Removed device ($i): ${device.toStringDebug()}")
                } ?: run { Log.w(TAG, "removedDevices are empty") }
        }
    }

    companion object {
        private val TAG = "HdmiCec!"

        private fun AudioDeviceInfo.toStringDebug(): String {
            return "name: $productName, type: ${type.describeDeviceType()}, sink: $isSink, source: $isSource"
        }

        private fun Int.describeDeviceType(): String = when (this) {
            19 -> "TYPE_AUX_LINE"
            26 -> "TYPE_BLE_HEADSET"
            27 -> "TYPE_BLE_SPEAKER"
            8 -> "TYPE_BLUETOOTH_A2DP"
            7 -> "TYPE_BLUETOOTH_SCO"
            1 -> "TYPE_BUILTIN_EARPIECE"
            15 -> "TYPE_BUILTIN_MIC"
            2 -> "TYPE_BUILTIN_SPEAKER"
            24 -> "TYPE_BUILTIN_SPEAKER_SAFE"
            21 -> "TYPE_BUS"
            13 -> "TYPE_DOCK"
            14 -> "TYPE_FM"
            16 -> "TYPE_FM_TUNER"
            9 -> "TYPE_HDMI"
            10 -> "TYPE_HDMI_ARC"
            29 -> "TYPE_HDMI_EARC"
            23 -> "TYPE_HEARING_AID"
            20 -> "TYPE_IP"
            5 -> "TYPE_LINE_ANALOG"
            6 -> "TYPE_LINE_DIGITAL"
            25 -> "TYPE_REMOTE_SUBMIX"
            18 -> "TYPE_TELEPHONY"
            17 -> "TYPE_TV_TUNER"
            0 -> "TYPE_UNKNOWN"
            12 -> "TYPE_USB_ACCESSORY"
            11 -> "TYPE_USB_DEVICE"
            22 -> "TYPE_USB_HEADSET"
            4 -> "TYPE_WIRED_HEADPHONES"
            3 -> "TYPE_WIRED_HEADSET"
            else -> "UNKNOWN"
        }
            .let { "$this - $it" }
    }
}

class HdmiEventReceiver : BroadcastReceiver() {

    private var callback: (extraAudioPlugState: String) -> Unit = {}

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        if (intent.action == ACTION_HDMI_AUDIO_PLUG) {
            val extraAudioPlugState = intent.getIntExtra(EXTRA_AUDIO_PLUG_STATE, Int.MIN_VALUE).describeState()
            callback(extraAudioPlugState)
        }
    }

    @CheckResult
    fun attach(context: Context, callback: (extraAudioPlugState: String) -> Unit): IDisposable {
        this.callback = callback
        val filter = IntentFilter().apply { addAction(ACTION_HDMI_AUDIO_PLUG) }
        context.registerReceiver(this, filter)
        return IDisposable {
            context.unregisterReceiver(this)
            this.callback = {}
        }
    }

    companion object {

        private fun Int.describeState(): String = when(this) {
            0 -> "UNPLUGGED"
            1 -> "PLUGGED"
            else -> "UNKNOWN"
        }.let { "$this - $it" }
    }
}

fun interface IDisposable {
    fun dispose()
}
