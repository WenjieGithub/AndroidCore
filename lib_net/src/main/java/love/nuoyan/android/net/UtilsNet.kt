package love.nuoyan.android.net

import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.LiveData

object UtilsNet : LiveData<NetStatus>() {
    const val transportWifi = "Wifi"
    const val transportOther = "Other"
    const val transportCellular = "Cellular"

    internal fun initNet(context: Context) {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connMgr?.registerDefaultNetworkCallback(networkCallback)
        } else {
            connMgr?.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
        value = NetStatus.Linked.apply { validated = true }
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
            val v = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (NetStatus.Linked.validated != v) {
                NetStatus.Linked.validated = v
                postValue(NetStatus.Linked)
            }
        }
    }

    fun isConnected() : Boolean {
        return value?.validated ?: false
    }
    /** 检查当前WIFI是否打开 */
    fun wifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifiManager?.isWifiEnabled == true
    }
    /** 当前网络是否有效 */
    fun isNetworkAvailable(): Boolean {
        return value == NetStatus.Linked && value?.validated == true
    }
}

enum class NetStatus {
    Linked, Disconnected;

    internal var validated = false
    internal var transport = UtilsNet.transportOther

    internal fun initStatus() {
        validated = false
        transport = UtilsNet.transportOther
    }

    fun isEffective() : Boolean {
        return validated
    }

    fun getTransport() : String {
        return transport
    }
}