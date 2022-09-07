package love.nuoyan.android.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.LiveData
import love.nuoyan.android.net.UtilsNet.transportOther

@SuppressLint("StaticFieldLeak")
object UtilsNet : LiveData<NetStatus>() {
    const val transportWifi = "Wifi"
    const val transportCellular = "Cellular"
    const val transportOther = "Other"
    private lateinit var mContext: Context

    internal fun initNet(context: Context) {
        mContext = context.applicationContext
        val connMgr = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connMgr?.registerDefaultNetworkCallback(networkCallback)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connMgr?.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
        value = NetStatus.Disconnected
    }

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(NetStatus.Linked)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            NetStatus.Linked.initStatus()
            postValue(NetStatus.Disconnected)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetStatus.Linked.transport = transportWifi
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetStatus.Linked.transport = transportCellular
                else -> NetStatus.Linked.transport = transportOther
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val v = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (NetStatus.Linked.validated != v) {
                    NetStatus.Linked.validated = v
                    postValue(NetStatus.Linked)
                }
            } else if (NetStatus.Linked.validated != isConnected()) {
                NetStatus.Linked.validated = !NetStatus.Linked.validated
                postValue(NetStatus.Linked)
            }
        }
    }

    fun isConnected() : Boolean {
        val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    /** 检查当前WIFI是否打开 */
    fun wifiEnabled(): Boolean {
        val wifiManager = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifiManager?.isWifiEnabled == true
    }
    /** 当前网络是否有效 */
    fun isNetworkAvailable(): Boolean {
        return value == NetStatus.Linked && value?.isEffective() == true
    }
}

enum class NetStatus {
    Linked, Disconnected;

    internal var validated = false
    internal var transport = transportOther
    internal fun initStatus(): NetStatus {
        validated = false
        transport = transportOther
        return this
    }

    fun isEffective() : Boolean {
        return validated
    }

    fun getTransport() : String {
        return transport
    }
}