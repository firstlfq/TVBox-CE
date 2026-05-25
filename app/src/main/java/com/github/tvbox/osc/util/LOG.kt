package com.github.tvbox.osc.util

import android.util.Log

import com.github.tvbox.osc.event.LogEvent

import org.greenrobot.eventbus.EventBus

object LOG {
    private var TAG = "TVBox"

    @JvmStatic
    fun e(t: Throwable) {
        Log.e(TAG, t.message, t)
        EventBus.getDefault().post(LogEvent(String.format("【E/%s】=>>>", TAG) + Log.getStackTraceString(t)))
    }

    @JvmStatic
    fun e(tag: String, t: Throwable) {
        Log.e(tag, t.message, t)
        EventBus.getDefault().post(LogEvent(String.format("【E/%s】=>>>", tag) + Log.getStackTraceString(t)))
    }

    @JvmStatic
    fun e(msg: String) {
        Log.e(TAG, "" + msg)
        EventBus.getDefault().post(LogEvent(String.format("【E/%s】=>>>", TAG) + msg))
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
        EventBus.getDefault().post(LogEvent(String.format("【E/%s】=>>>", tag) + msg))
    }

    @JvmStatic
    fun i(msg: String) {
        Log.i(TAG, msg)
        EventBus.getDefault().post(LogEvent(String.format("【I/%s】=>>>", TAG) + msg))
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        EventBus.getDefault().post(LogEvent(String.format("【I/%s】=>>>", tag) + msg))
    }
}
