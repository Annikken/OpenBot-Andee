/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: 2020-12-27, 10:57 p.m.
 */

package com.andee_ai_openbot.controller.customComponents

import android.content.Context
import android.util.AttributeSet
import android.util.Log

class SwitchCamera @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {
    init {
        setOnTouchListener(OnTouchListener("{command: SWITCH_CAMERA}"))
        subscribe("SWITCH_CAMERA", ::onDataReceived)
    }

    private fun onDataReceived(data: String) {
        Log.i("SwitchCamera", "Got SWITCH_CAMERA status...")
    }
}
