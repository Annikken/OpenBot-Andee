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

class DriveModeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {

    init {
        setOnTouchListener(OnTouchListener("{command: DRIVE_MODE}"))

        subscribe("DRIVE_MODE", ::onDataReceived)
        offState()
    }

    private fun onDataReceived(data: String) {
        text = data
    }
}
