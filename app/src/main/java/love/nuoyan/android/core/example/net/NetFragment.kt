package love.nuoyan.android.core.example.net

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import love.nuoyan.android.core.example.R
import love.nuoyan.android.core.example.databinding.LibNetLayoutBinding
import love.nuoyan.android.net.NetService
import love.nuoyan.android.net.NetStatus
import love.nuoyan.android.net.UtilsNet

class NetFragment : Fragment() {
    private lateinit var mBinding: LibNetLayoutBinding
    val code = MutableLiveData<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.lib_net_layout, container, false)
        mBinding.fragment = this
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NetService.init(requireContext()) {
            Log.e("NetCallback", it)
        }

        UtilsNet.observe(viewLifecycleOwner) {
            when (it) {
                NetStatus.Linked -> {
                    Log.e("NetStatus", NetStatus.Linked.name)
                    Log.e("NetStatus", NetStatus.Linked.isEffective().toString())
                    Log.e("NetStatus", NetStatus.Linked.getTransport())
                }
                NetStatus.Disconnected -> Log.e("Net", NetStatus.Disconnected.name)
                else -> Log.e("NetStatus", "NetStatus == null")
            }
        }
    }

    fun click() {
        lifecycleScope.launch(Main) {
            try {
                val result = NetService.get<String>("https://z1.m1907.cn/?jx=https://www.iqiyi.com/v_10zknhe8mjc.html").build()
                mBinding.text.text = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}