/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: 2020-12-27, 10:59 p.m.
 */

package com.andee_ai_openbot.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.andee_ai_openbot.controller.customComponents.DualDriveSeekBar
import com.andee_ai_openbot.controller.customComponents.VideoViewWebRTC
import org.openbot.controller.databinding.ActivityFullscreenBinding
import com.andee_ai_openbot.controller.utils.LocalEventBus
import com.andee_ai_openbot.controller.utils.Utils
import org.openbot.controller.R
import kotlin.system.exitProcess

class ControllerActivity : /*AppCompat*/
    Activity() { // for some reason AppCompatActivity gives errors in the IDE, but it does compile,
    private val PERMISSION_REQUEST_LOCATION = 101
    private val TAG = "ControllerActivity"
    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var screenSelector: ScreenSelector

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        binding = ActivityFull
        setContentView(binding.root)

        setupPermissions()

        screenSelector = ScreenSelector(binding)
        com.andee_ai_openbot.controller.ConnectionSelector.init(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        createAppEventsSubscription()
        subscribeToStatusInfo()

        binding.leftDriveControl.setDirection(DualDriveSeekBar.LeftOrRight.LEFT)
        binding.
        binding.rightDriveControl.setDirection(DualDriveSeekBar.LeftOrRight.RIGHT)

        screenSelector.hideControls()

        hideSystemUI()

        com.andee_ai_openbot.controller.BotDataListener.init()

        subscribe("VIDEO_PROTOCOL", ::onDataReceived)
    }

    @SuppressLint("CheckResult")
    private fun subscribe(subject: String, onDataReceived: (String) -> Unit) {
        StatusEventBus.addSubject(subject)
        StatusEventBus.subscribe(this.javaClass.simpleName, subject, onNext = {
            onDataReceived(it as String)
        })
    }

    private fun onDataReceived(data: String) {
        // Create the type of video view programmatically based on info from the Bot app.
        when (data) {
            "WEBRTC" -> {
                val view: VideoViewWebRTC = createView(VideoViewWebRTC(this)) as VideoViewWebRTC
                view.init()
            }
            "RTSP" -> {
                Toast.makeText(
                    this,
                    "RTSP not supported by this controller. For video, set your main app to use WebRTC.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createView(view: View): View {
        view.id = R.id.video_view

        val layoutParams = ViewGroup.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        view.layoutParams = layoutParams

        if (binding.video.childCount > 0) {
            binding.video.removeAllViews()
        }
        binding.video.addView(view)
        return view
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to get location denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_LOCATION
        )
    }

    @SuppressLint("CheckResult")
    private fun subscribeToStatusInfo() {
        StatusEventBus.addSubject("CONNECTION_ACTIVE")
        StatusEventBus.subscribe(this.javaClass.simpleName, "CONNECTION_ACTIVE", onNext = {
            if (it.toBoolean()) {
                screenSelector.showControls()
            } else {
                screenSelector.hideControls()
                binding.controlModeTiltLayout.stop()
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun createAppEventsSubscription() {
        LocalEventBus.subscriber.start(
            this.javaClass.simpleName,

            {
                Log.i(TAG, "Got $it event")

                when (it) {
                    LocalEventBus.ProgressEvents.ConnectionSuccessful -> {
                        Utils.beep()
                        screenSelector.showControls()
                    }
                    LocalEventBus.ProgressEvents.ConnectionStarted -> {
                    }
                    LocalEventBus.ProgressEvents.ConnectionFailed -> {
                        screenSelector.hideControls()
                    }
                    LocalEventBus.ProgressEvents.StartAdvertising -> {
                        screenSelector.hideControls()
                    }
                    LocalEventBus.ProgressEvents.Disconnected -> {
                        screenSelector.hideControls()
                        binding.controlModeTiltLayout.stop()
                    }
                    LocalEventBus.ProgressEvents.StopAdvertising -> {
                    }
                    LocalEventBus.ProgressEvents.TemporaryConnectionProblem -> {
                        screenSelector.hideControls()
                        com.andee_ai_openbot.controller.ConnectionSelector.getConnection().connect(this)
                    }
                    LocalEventBus.ProgressEvents.PhoneOnTable -> {
                        screenSelector.showControls()
                    }
                    LocalEventBus.ProgressEvents.AdvertisingFailed -> {
                        screenSelector.hideControls()
                    }
                }
            },
            { throwable ->
                Log.d(
                    "EventsSubscription",
                    "Got error on subscribe: $throwable"
                )
            })
    }

    private fun hideSystemUI() {
            window.insetsController.let {
                it?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                window.navigationBarColor = getColor(R.color.colorPrimaryDark)
                it?.hide(WindowInsets.Type.systemBars())
            }
    }

    @Override
    override fun onPause() {
        super.onPause()
        com.andee_ai_openbot.controller.ConnectionSelector.getConnection().disconnect()
    }

    @Override
    override fun onResume() {
        super.onResume()
        hideSystemUI()

        com.andee_ai_openbot.controller.ConnectionSelector.getConnection().init(this)
        com.andee_ai_openbot.controller.ConnectionSelector.getConnection().connect(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                    finish()
                    exitProcess(0)
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
        }
    }

    companion object
}
