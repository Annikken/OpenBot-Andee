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
import androidx.core.content.ContextCompat
import org.openbot.controller.R
import org.openbot.controller.StatusEventBus

class ConnectionActiveIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : com.google.android.material.imageview.ShapeableImageView (context, attrs, defStyleAttr) {

    init {
        offState()
        subscribe("CONNECTION_ACTIVE", ::onDataReceived)
    }

    private fun onDataReceived(data: String) {
        setOnOffStateConditions(data)
    }

    private fun offState() {
        setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun onState() {
        setColorFilter(ContextCompat.getColor(context, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun subscribe(subject: String, onDataReceived: (String) -> Unit) {
        StatusEventBus.addSubject(subject)
        StatusEventBus.subscribe(this.javaClass.simpleName, subject, onNext = {
            onDataReceived(it as String)
        })
    }

    private fun setOnOffStateConditions(value: String) {
        if (value == "true") onState() else offState()
    }
}
