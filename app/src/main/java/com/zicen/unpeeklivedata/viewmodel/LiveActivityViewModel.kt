package com.zicen.unpeeklivedata.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zicen.unpeeklivedata.livedata.BusLiveData
import com.zicen.unpeeklivedata.livedata.EventLiveData
import com.zicen.unpeeklivedata.livedata.UnPeekLiveData

/**
 * m.poizon.com Inc.
 * Copyright (c) 1999-2020 All Rights Reserved.
 *
 * @author lizhenquan
 * @contact lizhenquan@theduapp.com
 */
class LiveActivityViewModel : ViewModel() {
    val unPeekLiveData = UnPeekLiveData<String>()
    val busLiveData = BusLiveData<String>()
    val eventLiveData = EventLiveData<String>()
    val mutableLiveData = MutableLiveData<String>()
}
