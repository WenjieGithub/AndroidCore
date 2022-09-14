package love.nuoyan.android.qr.decode

import com.google.zxing.BarcodeFormat
import java.util.*

// 用于存放解码类型分类
object DecodeFormatManager {
    // 产品格式
    val PRODUCT_FORMATS = EnumSet.of(
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.RSS_14,
        BarcodeFormat.RSS_EXPANDED
    )
    // 工业格式
    val INDUSTRIAL_FORMATS = EnumSet.of(
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128,
        BarcodeFormat.ITF,
        BarcodeFormat.CODABAR
    )
    val ONE_D_FORMATS = EnumSet.copyOf(PRODUCT_FORMATS)
    val QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE)
    val DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX)

    init {
        ONE_D_FORMATS.addAll(INDUSTRIAL_FORMATS)
    }
}
