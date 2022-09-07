package love.nuoyan.android.floating_kit.ui

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import love.nuoyan.android.floating_kit.extension.Extension
import love.nuoyan.android.floating_kit.R

class KitAppInfoFragment : Fragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lib_floating_kit_app_info_layout, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener(this)
        view.findViewById<View>(R.id.kit_back).setOnClickListener(this)

        try {
            view.findViewById<TextView>(R.id.kit_app_package_value).text = Extension.application.packageName
            view.findViewById<TextView>(R.id.kit_app_version_value).text = Extension.getVersionName()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.findViewById<TextView>(R.id.kit_app_min_version_value).text = Extension.application.applicationInfo.minSdkVersion.toString()
            }
            view.findViewById<TextView>(R.id.kit_app_target_version_value).text = Extension.application.applicationInfo.targetSdkVersion.toString()

            view.findViewById<TextView>(R.id.kit_app_phone_model_value).text = "${Build.MANUFACTURER} ${Build.MODEL}"
            view.findViewById<TextView>(R.id.kit_app_system_version_value).text = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})"
            view.findViewById<TextView>(R.id.kit_app_resolution_value).text = "${Extension.getScreenWidth()} x ${Extension.getScreenRealHeight(requireActivity().windowManager)}"
            view.findViewById<TextView>(R.id.kit_app_screen_size_value).text = Extension.getScreenInch(requireActivity()).toString()
            view.findViewById<TextView>(R.id.kit_app_density_value).text = Resources.getSystem().displayMetrics.density.toString()
            view.findViewById<TextView>(R.id.kit_app_root_value).text = Extension.isDeviceRooted().toString()

            view.findViewById<TextView>(R.id.kit_app_ip_value).text = Extension.getIPAddress(true)
            view.findViewById<TextView>(R.id.kit_app_sign_md5_value).text = Extension.getAppSignatureHash(
                Extension.application.packageName, "MD5")
            view.findViewById<TextView>(R.id.kit_app_sign_sha1_value).text = Extension.getAppSignatureHash(
                Extension.application.packageName, "SHA1")
            view.findViewById<TextView>(R.id.kit_app_sign_sha256_value).text = Extension.getAppSignatureHash(
                Extension.application.packageName, "SHA256")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.kit_back -> requireActivity().supportFragmentManager.popBackStack()
        }
    }
}