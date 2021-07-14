package com.zicen.unpeeklivedata.livedata;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * m.poizon.com Inc.
 * Copyright (c) 1999-2020 All Rights Reserved.
 *  此种方式参考美团: https://tech.meituan.com/2018/07/26/android-livedatabus.html
 *  美团是通过反射来修改 LiveData 的 version，此方式是通过自定义 LiveData 维护 version 防止数据倒灌，比美团方案更优雅。
 *
 * @author lizhenquan
 * @contact lizhenquan@theduapp.com
 */
public class BusLiveData<T> extends MutableLiveData<T> {

    private static final int VERSION_START = -1;

    private int activeVersion = VERSION_START;
    private ArrayMap<Observer<? super T>, BusObserver<? super T>> busObservers = new ArrayMap();

    @Override
    public void setValue(T value) {
        activeVersion++;
        super.setValue(value);
    }

    private BusObserver<? super T> createBusObserver(@NonNull Observer<? super T> observer, int latestVersion) {
        BusObserver<? super T> busObserver = busObservers.get(observer);
        if (busObserver == null) {
            busObserver = new BusObserver(observer, latestVersion);
            busObservers.put(observer, busObserver);
        } else {
            throw new IllegalArgumentException("Please not register same observer " + observer);
        }
        return busObserver;
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }
        super.observe(owner, createBusObserver(observer, activeVersion));
    }

    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }
        super.observe(owner, createBusObserver(observer, VERSION_START));
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        super.observeForever(createBusObserver(observer, activeVersion));
    }

    public void observeStickyForever(@NonNull Observer<T> observer) {
        super.observeForever(createBusObserver(observer, VERSION_START));
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        BusObserver<? super T> busObserver;
        if (observer instanceof BusObserver) {
            busObserver = (BusObserver) observer;
        } else {
            busObserver = busObservers.get(observer);
        }
        if (busObserver != null) {
            busObservers.remove(busObserver.realObserver);
            super.removeObserver(busObserver);
        }
    }

    private class BusObserver<M extends T> implements Observer<M> {
        private Observer<M> realObserver;
        private int lastVersion;

        public BusObserver(@NonNull Observer<M> realObserver, int lastVersion) {
            this.realObserver = realObserver;
            this.lastVersion = lastVersion;
        }

        @Override
        public void onChanged(M m) {
            // 举个例子，在直播间上下滑场景中，此 LiveData 此时有旧数据，然后切换到一个 Fragment 中，这个新切换到的 Fragment initData 的时候观察此 LiveData
            // 然后此时 activeVersion = lastVersion（看 BusObserver 在 observe 方法中创建的时候，构造函数 lastVersion 传的就是 activeVersion）
            // 也就是会直接 return
            if (activeVersion <= lastVersion) {
                return;
            }
            lastVersion = activeVersion;
            if (m != null) {
                realObserver.onChanged(m);
            }
        }
    }

}
