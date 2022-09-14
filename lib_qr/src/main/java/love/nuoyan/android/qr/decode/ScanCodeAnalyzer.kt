package love.nuoyan.android.qr.decode

import android.app.Activity
import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

class ScanCodeAnalyzer(
    val callback: ((code: Int, data: String) -> Unit),
    private val isPlayAudio: Boolean = false,
    private val activity: Activity? = null
) : ImageAnalysis.Analyzer {
    private val reader: MultiFormatReader = initReader()

    override fun analyze(image: ImageProxy) {
        try {
            if (ImageFormat.YUV_420_888 != image.format) {
                callback(-1, "expect YUV_420_888, now = ${image.format}")
                return
            }
            // 将buffer数据写入数组
            val data = image.planes[0].buffer.toByteArray()

            // 图片宽高
            val height = image.height
            val width = image.width

            // 将图片旋转
            val rotationData = ByteArray(data.size)
            var j: Int
            var k: Int
            for (y in 0 until height) {
                for (x in 0 until width) {
                    j = x * height + height - y - 1
                    k = x + y * width
                    rotationData[j] = data[k]
                }
            }
            val source = PlanarYUVLuminanceSource(rotationData, height, width, 0, 0, height, width, false)
            val bitmap = BinaryBitmap(HybridBinarizer(source))

            val result = reader.decode(bitmap)
            if (isPlayAudio && activity != null) {
                AudioUtil.playBeepSoundAndVibrate(activity)
            }
            callback(0, result.text)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }

    // 将buffer写入数组
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    private fun initReader(): MultiFormatReader {
        val formatReader = MultiFormatReader()
        val hints = Hashtable<DecodeHintType, Any>()
        val decodeFormats = Vector<BarcodeFormat>()

        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        hints[DecodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8
//        hints[DecodeHintType.TRY_HARDER] = true
//        hints[DecodeHintType.PURE_BARCODE] = true
        formatReader.setHints(hints)
        return formatReader
    }
}