package com.zicen.unpeeklivedata.livedata

import android.os.Looper
import android.os.SystemClock
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 文档：https://duapp.yuque.com/team_tech/tk26sp/uegoi4
 * 用于事件型场景时：点击事件等，建议使用此 LiveData
 * 1. 避免了数据倒灌，通过 key 来确定是否消费过事件，默认 key = "${owner::class.qualifiedName}"，一个事件只能被一个 key 消费一次
 * 2. 可以自定义是否粘性，默认粘性。
 *
 * @param sticky 是否是粘性，默认粘性。设置非粘性时，只能在 `liveData.observe()` 之后调用 `liveData.setValue()`
 */
class EventLiveData<T>(val sticky: Boolean = true) : MutableLiveData<T>() {
    private val tempValueMap = ArrayMap<String, Any?>(2)
    private val foreverObserverMap = ArrayMap<String, Observer<T>>(2)
    private val callMap = ArrayMap<String, Int>(2)
    private var isObservedMap = ArrayMap<String, Boolean>(2)
    private val callCount = AtomicInteger(0)

    /**
     * 最后一个事件是否是 [call] 发出的
     */
    var lastIsCall: Boolean? = null
        private set

    @Volatile
    private var invokePostValueFromCall = false

    /**
     * 直接发送一个 null 值来通知 [Observer] 回调，必须通过 [observe]/[observeForever] 注册才能收到结果，如果通过 [observeNonNull]/[observeForeverNonNull]
     * 注册会抛出错误。
     */
    fun call() {
        callCount.incrementAndGet()
        setCallForAll()
        lastIsCall = true
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.setValue(null)
        } else {
            invokePostValueFromCall = true
            super.postValue(null)
        }
    }

    override fun postValue(value: T?) {
        invokePostValueFromCall = false
        super.postValue(value)
    }

    override fun setValue(value: T?) {
        lastIsCall = invokePostValueFromCall
        invokePostValueFromCall = false
        setValueForAll(value)
        super.setValue(value)
    }

    private fun setCallForAll() {
        for (item in tempValueMap) {
            // 跳过非粘性还没注册的组件
            val isObserved = isObservedMap[item.key] ?: false
            if (!isObserved && !sticky) {
                continue
            }
            item.setValue(CALL)
        }
    }

    private fun setValueForAll(value: T?) {
        for (item in tempValueMap) {
            // 跳过非粘性还没注册的组件
            val isObserved = isObservedMap[item.key] ?: false
            if (!isObserved && !sticky) {
                continue
            }
            item.setValue(value ?: NULL)
        }
    }

    /**
     * @param owner
     * @param key an event only can be consumed once by same key
     * @param observer
     */
    @MainThread
    fun observe(
        owner: LifecycleOwner,
        key: String = getKey(owner),
        observer: Observer<in T>,
    ) {
        onObserve(owner, key)
        super.observe(owner.get(), getObserverWrapper(key, observer))
    }

    override fun observe(
        owner: LifecycleOwner,
        observer: Observer<in T>,
    ) {
        observe(owner, getKey(owner), observer)
    }

    inline fun observe(
        owner: LifecycleOwner,
        key: String? = null,
        crossinline block: ((T?) -> Unit),
    ) {
        observe(owner, key ?: getKey(owner), Observer {
            block(it)
        })
    }

    /**
     * 观察非 null 的值，对 [call] 不适用
     */
    inline fun observeNonNull(
        owner: LifecycleOwner,
        key: String? = null,
        crossinline block: ((T) -> Unit),
    ) {
        observe(owner, key ?: getKey(owner), Observer {
            require(lastIsCall == false) {
                "observeNonNull unSupport observe Call, use observe"
            }
            block(it ?: return@Observer)
        })
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        observeForever(null, observer.toString(), observer)
    }

    @MainThread
    fun observeForever(
        owner: LifecycleOwner? = null,
        key: String? = null,
        observer: Observer<in T>,
    ) {
        val k = key ?: getKey(owner)
        onObserve(owner, k)
        super.observeForever(getObserverWrapper(k, observer).also {
            foreverObserverMap[key] = it
        })
    }

    inline fun observeForever(
        owner: LifecycleOwner?,
        key: String? = null,
        crossinline block: ((T?) -> Unit),
    ) {
        observeForever(owner, key, Observer {
            require(lastIsCall == false) {
                "observeNonNull unSupport observe Call, use observe"
            }
            block(it)
        })
    }

    inline fun observeForeverNonNull(
        owner: LifecycleOwner?,
        key: String? = null,
        crossinline block: ((T) -> Unit),
    ) {
        observeForever(owner, key, Observer {
            require(lastIsCall == false) {
                "observeNonNull unSupport observe Call, use observe"
            }
            block(it ?: return@Observer)
        })
    }

    fun removeObserver(key: String? = null, observer: Observer<in T>) {
        super.removeObserver(observer)
        remove(key ?: observer.toString())
    }

    override fun removeObserver(observer: Observer<in T>) {
        super.removeObserver(observer)
        remove(observer.toString())
    }

    fun LifecycleOwner.get() = if (this is Fragment && view != null) viewLifecycleOwner else this

    private fun onObserve(
        owner: LifecycleOwner?,
        key: String,
    ) {
        (owner?.get())?.lifecycle?.addObserver(OnDestroyLifecycleObserver(this, key))
        isObservedMap[key] = true
        if (tempValueMap[key] == null) {
            tempValueMap[key] = if (!sticky || value == null) {
                UNSET
            } else {
                value
            }
        }
        // 最后一次是 setValue
        if (lastIsCall == false && sticky) {
            if (value == null) {
                tempValueMap[key] = NULL
            }
        }
        // 最后一次是 call 事件
        if (lastIsCall == true && callCount.get() > 0 && sticky) {
            val callValue = callMap[key] ?: 0
            if (callValue < callCount.get()) {
                tempValueMap[key] = CALL
            }
        }
    }

    private fun getObserverWrapper(
        key: String,
        observer: Observer<in T>,
    ): Observer<T> {
        return Observer<T> {
            if (tempValueMap[key] === UNSET) return@Observer
            @Suppress("UNCHECKED_CAST")
            observer.onChanged(value as T)
            // 消费完 call 事件就同步进度
            if (tempValueMap[key] === CALL) {
                callMap[key] = callCount.get()
            }
            tempValueMap[key] = UNSET
        }
    }

    fun getKey(owner: LifecycleOwner?) =
        if (owner != null) {
            owner::class.qualifiedName ?: owner::class.java.name
        } else {
            SystemClock.currentThreadTimeMillis().toString()
        }

    private fun onClear(key: String) {
        val observer = foreverObserverMap[key]
        if (observer != null) {
            removeObserver(observer)
        } else {
            remove(key)
        }
    }

    private class OnDestroyLifecycleObserver<T>(
        private val livaData: EventLiveData<T>,
        private val key: String,
    ) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            livaData.onClear(key)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("\ntempValueMap = ")
        if (tempValueMap.isNotEmpty()) {
            tempValueMap.keys.forEachIndexed { index, key ->
                if (index == 0) {
                    sb.append("{")
                }
                sb.append(key)
                    .append(":")
                    .append(tempValueMap[key])
                if (index == tempValueMap.keys.size - 1) {
                    sb.append("}")
                } else {
                    sb.append(", ")
                }
            }
        } else {
            sb.append("{}")
        }

        sb.append("\nforeverObserverMap = ")
        if (foreverObserverMap.keys.isEmpty()) {
            sb.append("{}")
        } else {
            foreverObserverMap.keys.forEachIndexed { index, key ->
                if (index == 0) {
                    sb.append("{")
                }
                sb.append(key)
                    .append(":")
                    .append(foreverObserverMap[key])
                if (index == foreverObserverMap.keys.size - 1) {
                    sb.append("}")
                } else {
                    sb.append(", ")
                }
            }
        }

        sb.append("\nisObservedMap = ")
        if (isObservedMap.isEmpty) {
            sb.append("{}")
        } else {
            isObservedMap.keys.forEachIndexed { index, key ->
                if (index == 0) {
                    sb.append("{")
                }
                sb.append(key)
                    .append(":")
                    .append(isObservedMap[key])
                if (index == isObservedMap.keys.size - 1) {
                    sb.append("}")
                } else {
                    sb.append(", ")
                }
            }
        }
        val address = super.toString()
        return "$address:\n$sb"
    }

    private fun remove(key: String) {
        tempValueMap.remove(key)
        isObservedMap.remove(key)
        callMap.remove(key)
        foreverObserverMap.remove(key)
    }

    companion object {
        private val UNSET = Any()
        private val NULL = Any()
        private val CALL = Any()
        private const val TAG = "EventLiveData"
    }
}

