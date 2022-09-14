package love.nuoyan.android.core.example.qr

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import love.nuoyan.android.core.example.R
import love.nuoyan.android.core.example.databinding.LibQrLayoutBinding
import love.nuoyan.android.core.example.dp2px
import love.nuoyan.android.qr.UtilsCode
import love.nuoyan.android.permission.PermissionRequest

class QrFragment : Fragment() {
    private lateinit var mBinding: LibQrLayoutBinding
    val code = MutableLiveData<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.lib_qr_layout, container, false)
        mBinding.fragment = this
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    fun clickScanCode() {
        PermissionRequest(this) {
            if (it.isSuccess) {
                UtilsCode.scanCode(requireContext(), { result ->
                    code.postValue(result.getOrNull() ?: result.exceptionOrNull()?.message)
                })
            }
        }.applyPermission(Manifest.permission.CAMERA)
    }

    fun clickBuildCode() {
        // 必传参数（text:要生成的二维码内容）
        val bitmap = UtilsCode.createQRCode(
            "1234abcd",
            200.dp2px.toInt(),
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
            60.dp2px.toInt(),
            60.dp2px.toInt(),
            10.dp2px,
            10.dp2px,
            2.dp2px.toInt(),
            ContextCompat.getColor(requireContext(), R.color.purple_500),
            true
        )
        if (bitmap != null) {
            mBinding.image.setImageBitmap(bitmap)
        }
    }
}