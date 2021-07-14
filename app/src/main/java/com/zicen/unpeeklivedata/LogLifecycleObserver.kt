package com.zicen.unpeeklivedata

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * m.poizon.com Inc.
 * Copyright (c) 1999-2020 All Rights Reserved.
 *
 * @author lizhenquan
 * @contact lizhenquan@theduapp.com
 * 直播间生命周期日志打印工具
 */
class LogLifecycleObserver(val fragment: Fragment) : LifecycleObserver {
    companion object {
        const val TAG = "live_lifecycle"
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onHostCreate() {
        Log.d(TAG, "host: $fragment onHostCreate")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onHostStart() {
        Log.d(TAG, "host: $fragment onHostStart")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onHostResume() {
        Log.d(TAG, "host: $fragment onHostResume")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onHostPause() {
        Log.d(TAG, "host: $fragment onHostPause")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onHostStop() {
        Log.d(TAG, "host: $fragment onHostStop")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onHostDestroy() {
        Log.d(TAG, "host: $fragment onHostDestroy")
    }
}
