/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: 2020-12-27, 10:58 p.m.
 */

package com.andee_ai_openbot.controller

import org.json.JSONObject

/*
This class listens for status data from the Bot and emits events.
These events are received by various custom components which update their UI accordingly.
For example, a right indicator will start blinking if the status on the bot is set.
 */
object BotDataListener {

    fun init() {
        val dataReceived: com.andee_ai_openbot.controller.IDataReceived = object : com.andee_ai_openbot.controller.IDataReceived {
            override fun dataReceived(command: String?) {
                val dataJson = JSONObject(command as String)
                val statusValues = dataJson.getJSONObject("status")

                for (key in statusValues.keys()) {
                    val value: String = statusValues.getString(key)

                    /*
                    Send an event on a particular subject.
                    The custom components are listening on their subject.
                    */
                    com.andee_ai_openbot.controller.StatusEventBus.emitEvent(key, value)
                }
            }
        }

        com.andee_ai_openbot.controller.ConnectionSelector.getConnection().setDataCallback(dataReceived)
    }
}