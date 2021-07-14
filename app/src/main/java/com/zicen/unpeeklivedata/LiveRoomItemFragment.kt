package com.zicen.unpeeklivedata

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xuexiang.xui.widget.textview.LoggerTextView
import com.zicen.unpeeklivedata.viewmodel.LiveActivityViewModel
import com.zicen.unpeeklivedata.viewmodel.LiveRoomItemViewModel
import kotlinx.android.synthetic.main.fragment_live_room_item.*
import java.text.FieldPosition

class LiveRoomItemFragment : Fragment() {
    private val logLifecycle by lazy { LogLifecycleObserver(this) }
    private lateinit var viewModel: LiveRoomItemViewModel
    private val TAG = "LiveRoomItemFragment"
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(logLifecycle)
        position = arguments?.getInt("position", 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_room_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LiveRoomItemViewModel::class.java)

        subscribeUi()
        initClickListener()
    }

    private fun initClickListener() {
        btnChangeMutableLiveData.setOnClickListener {
            viewModel.mutableLiveData.value = "改变了 LiveData"
            val shareViewModel = activity?.let {
                val shareViewModel = ViewModelProvider(it).get(LiveActivityViewModel::class.java)
                shareViewModel
            } ?: return@setOnClickListener
            shareViewModel.mutableLiveData.value = "改变了 shareViewModel LiveData"
        }
        btnChangeBusLiveData.setOnClickListener {
            viewModel.busLiveData.value = "改变了 LiveData"
            val shareViewModel = activity?.let {
                val shareViewModel = ViewModelProvider(it).get(LiveActivityViewModel::class.java)
                shareViewModel
            } ?: return@setOnClickListener
            shareViewModel.busLiveData.value = "改变了 shareViewModel LiveData"
        }
        btnChangeEventLiveData.setOnClickListener {
            viewModel.eventLiveData.value = "改变了 LiveData"
            val shareViewModel = activity?.let {
                val shareViewModel = ViewModelProvider(it).get(LiveActivityViewModel::class.java)
                shareViewModel
            } ?: return@setOnClickListener
            shareViewModel.eventLiveData.value = "改变了 shareViewModel LiveData"
        }
        btnChangeUnPeekLiveData.setOnClickListener {
            viewModel.unPeekLiveData.value = "改变了 LiveData"
            val shareViewModel = activity?.let {
                val shareViewModel = ViewModelProvider(it).get(LiveActivityViewModel::class.java)
                shareViewModel
            } ?: return@setOnClickListener
            shareViewModel.unPeekLiveData.value = "改变了 shareViewModel LiveData"
        }
    }

    private fun subscribeUi() {
        viewModel.busLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "stateViewModel busLiveData: $it")
            loggerTextView.addLog("stateViewModel busLiveData: $it", LoggerTextView.LogType.NORMAL)
        })

        viewModel.mutableLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "stateViewModel mutableLiveData: $it")
            loggerTextView.addLog("stateViewModel mutableLiveData: $it", LoggerTextView.LogType.NORMAL)
        })

        viewModel.unPeekLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "stateViewModel unPeekLiveData: $it")
            loggerTextView.addLog("stateViewModel unPeekLiveData: $it", LoggerTextView.LogType.NORMAL)
        })

        viewModel.eventLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "stateViewModel eventLiveData: $it")
            loggerTextView.addLog("stateViewModel eventLiveData: $it", LoggerTextView.LogType.NORMAL)
        })
        val shareViewModel = activity?.let {
            val shareViewModel = ViewModelProvider(it).get(LiveActivityViewModel::class.java)
            shareViewModel
        } ?: return
        shareViewModel.busLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "shareViewModel busLiveData: $it ,position: $position")
            loggerTextView.addLog("shareViewModel busLiveData: $it ,position: $position",
                LoggerTextView.LogType.SUCCESS)
        })

        shareViewModel.mutableLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "shareViewModel mutableLiveData: $it ,position: $position")
            loggerTextView.addLog("shareViewModel mutableLiveData: $it ,position: $position",
                LoggerTextView.LogType.SUCCESS)
        })

        shareViewModel.unPeekLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "shareViewModel unPeekLiveData: $it ,position: $position")
            loggerTextView.addLog("shareViewModel unPeekLiveData: $it ,position: $position",
                LoggerTextView.LogType.SUCCESS)
        })

        shareViewModel.eventLiveData.observe(viewLifecycleOwner, {
            Log.i(TAG, "shareViewModel eventLiveData: $it ,position: $position")
            loggerTextView.addLog("shareViewModel eventLiveData: $it ,position: $position",
                LoggerTextView.LogType.SUCCESS)
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): LiveRoomItemFragment {
            val liveRoomItemFragment = LiveRoomItemFragment()
            val arg = Bundle()
            arg.putInt("position", position)
            liveRoomItemFragment.arguments = arg
            return liveRoomItemFragment
        }
    }
}
