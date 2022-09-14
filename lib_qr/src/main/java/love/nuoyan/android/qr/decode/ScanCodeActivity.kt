package love.nuoyan.android.qr.decode

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import love.nuoyan.android.qr.R
import love.nuoyan.android.qr.UtilsCode
import love.nuoyan.android.qr.widget.ScanView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanCodeActivity : AppCompatActivity() {
    private val mResultCode = 10011
    private val ratioValue43 = 4.0 / 3.0
    private val ratioValue169 = 16.0 / 9.0

    private lateinit var mPreview: Preview
    private lateinit var mImageAnalyzer: ImageAnalysis
    private lateinit var mImageCapture: ImageCapture

    private lateinit var mCamera: Camera
    private lateinit var mCameraInfo: CameraInfo
    private lateinit var mCameraControl: CameraControl
    private lateinit var mCameraExecutor: ExecutorService

    private var flashlight = false

    companion object {
        internal var callback: ((result: Result<String>) -> Unit)? = null
        internal var isPlayAudio: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()
        setContentView(R.layout.lib_qr_scan_layout)
        bindCameraUseCases()

        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.photo).setOnClickListener {
            val picIntent = Intent(Intent.ACTION_PICK)
            picIntent.type = "image/*"
            startActivityForResult(picIntent, mResultCode)
        }
        findViewById<View>(R.id.flashlight).setOnClickListener {
            flashlight = !flashlight
            setFlashStatus(flashlight)
            (it as? ImageView)?.setImageResource(if (flashlight) R.drawable.lib_qr_flashlight_open else R.drawable.lib_qr_flashlight_close)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mResultCode) {
            when (resultCode) {
                RESULT_OK -> {
                    if (data?.data == null) {
                        callbackFailure("${getString(R.string.lib_qr_photo_failure)}; uri = null")
                    } else {
                        try {
                            val content = UtilsCode.scanningImage(this, data.data!!)
                            if (content == null) {
                                callbackFailure("解析相册图片失败; content = null")
                            } else {
                                callbackSuccess(content)
                            }
                        } catch (e: Exception) {
                            callbackFailure("${getString(R.string.lib_qr_photo_failure)}; exception: ${e.stackTraceToString()}")
                        }
                    }
                }
                RESULT_CANCELED -> {
                    callbackFailure(getString(R.string.lib_qr_photo_canceled))
                }
                else -> {
                    callbackFailure("${getString(R.string.lib_qr_photo_failure)}; resultCode: $resultCode")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun callbackFailure(msg: String, isFinish: Boolean = true) {
        callback?.let { it(Result.failure(IllegalStateException(msg))) }
        if (isFinish) { finish() }
    }
    private fun callbackSuccess(data: String, isFinish: Boolean = true) {
        callback?.let { it(Result.success(data)) }
        if (isFinish) { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraExecutor.shutdownNow()
        findViewById<ScanView>(R.id.scan)?.cancelAnim()
        callback = null
    }

    private fun transparentStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun bindCameraUseCases() {
        mCameraExecutor = Executors.newSingleThreadExecutor()
        val pv = findViewById<PreviewView>(R.id.preview)
        // surface 准备监听 设置需要实现的用例（预览，拍照，图片数据解析等等）
        pv.post {
            // 获取使用的屏幕比例分辨率属性
            val screenAspectRatio = aspectRatio(pv.measuredWidth, pv.measuredHeight)
            val width = pv.measuredWidth
            val height = if (screenAspectRatio == AspectRatio.RATIO_16_9) {
                (width * ratioValue169).toInt()
            } else {
                (width * ratioValue43).toInt()
            }
            val size = Size(width, height)
            // 获取旋转角度
            val rotation = pv.display.rotation
            // 生命周期绑定  设置所选相机
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                // 预览用例
                mPreview = Preview.Builder()
                    .setTargetResolution(size)
                    .setTargetRotation(rotation)
                    .build()
                // 图像捕获用例
                mImageCapture = ImageCapture.Builder()
                    .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                // 图像分析用例
                mImageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(size)
                    .setTargetRotation(rotation)
                    .build()
                    .apply {
                        setAnalyzer(mCameraExecutor, ScanCodeAnalyzer({ code, data ->
                            if (code == 0) {
                                this@ScanCodeActivity.finish()
                                clearAnalyzer()
                                callbackSuccess(data, false)
                            }
                        }, isPlayAudio, this@ScanCodeActivity))
                    }
                // 必须在重新绑定用例之前取消之前绑定
                cameraProvider.unbindAll()
                try {
                    // 获取相机实例
                    mCamera = cameraProvider.bindToLifecycle(this, cameraSelector, mPreview, mImageCapture, mImageAnalyzer)
                    // 设置预览的view
                    mPreview.setSurfaceProvider(pv.surfaceProvider)
                    mCameraControl = mCamera.cameraControl
                    mCameraInfo = mCamera.cameraInfo
                    bindTouchListener()
                    setFlashStatus(flashlight)
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindTouchListener() {
        val zoomState = mCameraInfo.zoomState
        val ptl = PreviewTouchListener(this)
        ptl.setCustomTouchListener { delta ->
            zoomState.value?.let {
                val currentZoomRatio = it.zoomRatio
                mCameraControl.setZoomRatio(currentZoomRatio * delta)
            }
        }
        findViewById<PreviewView>(R.id.preview).setOnTouchListener(ptl)
    }

    private fun setFlashStatus(isOpenFlash: Boolean) {
        mCameraControl.enableTorch(isOpenFlash)
    }

    // 根据传入的值获取相机应该设置的分辨率比例
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)
        if (kotlin.math.abs(previewRatio - ratioValue43) <= kotlin.math.abs(previewRatio - ratioValue169)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }
}