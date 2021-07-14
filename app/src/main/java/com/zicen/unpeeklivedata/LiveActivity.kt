package com.zicen.unpeeklivedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.zicen.unpeeklivedata.R.id.viewPager2
import com.zicen.unpeeklivedata.viewmodel.LiveActivityViewModel

class LiveActivity : AppCompatActivity() {
    private val viewPager2: ViewPager2 by lazy { findViewById(R.id.viewPager2) }
    private lateinit var shareViewModel : LiveActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)
        shareViewModel = ViewModelProvider(this).get(LiveActivityViewModel::class.java)

        viewPager2.adapter = LiveRoomAdapter(this)
        viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
    }
}
