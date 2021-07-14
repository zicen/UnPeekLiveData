package com.zicen.unpeeklivedata

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * m.poizon.com Inc.
 * Copyright (c) 1999-2020 All Rights Reserved.
 *
 * @author lizhenquan
 * @contact lizhenquan@theduapp.com
 */
class LiveRoomAdapter(private val activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 100
    }

    override fun createFragment(position: Int): Fragment {
        return LiveRoomItemFragment.newInstance(position)
    }
}
