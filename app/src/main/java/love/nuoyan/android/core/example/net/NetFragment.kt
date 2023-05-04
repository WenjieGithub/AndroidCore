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
import love.nuoyan.android.net.UtilsJson
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
                NetStatus.Disconnected -> Log.e("NetStatus", NetStatus.Disconnected.name)
                else -> Log.e("NetStatus", "NetStatus = null")
            }
        }
    }

    fun click() {
        lifecycleScope.launch(Main) {
            val url = "https://gateway.caixin.com/api/app-api/password/update"//点击查看详情次数统计
            val r = NetService.postJson<String>(url).apply {
                val map = HashMap<String, Any>()
                map["id"] = "commandId"
                map["clickNum"] = 1
                UtilsJson.toJson(map.toMap())?.let { paramsJson(it) }
            }.build(onError = {
                it.toString()
            })



//            val result = NetService.get<String>("https://madminv5pre.caixin.com/tmp/channelv5/list_8_20_1.json?")
//                .call(onError = {
//                    it.toString()
//                })
            mBinding.text.text = r
        }
    }
}