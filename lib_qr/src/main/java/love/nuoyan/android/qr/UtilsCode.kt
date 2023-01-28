package love.nuoyan.android.qr

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import love.nuoyan.android.qr.decode.ScanCodeActivity
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import kotlin.Result
import kotlin.math.ceil

object UtilsCode {
    /**
     * 用相机扫描二维码，使用前请求权限
     */
    fun scanCode(context: Context, callback: (result: Result<String>) -> Unit, isPlayAudio: Boolean = false ) {
        ScanCodeActivity.callback = callback
        ScanCodeActivity.isPlayAudio = isPlayAudio
        val intent = Intent(context, ScanCodeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 生成二维码, 可添加 logo
     * @param text                  文字
     * @param size                  二维码大小 1 ：1 , 默认大小为 500
     * @param logo                  logo, 为空则不添加logo, 默认为 空
     * @param logoWith              logo宽 默认大小为 100
     * @param logoHigh              logo高 默认大小为 100
     * @param logoRadiusX           logo x圆角 默认大小为 0
     * @param logoRadiusY           logo y圆角 默认大小为 0
     * @param strokeWith            描边宽度 默认大小为 0
     * @param strokeColor           描边颜色 默认颜色为 透明
     * @param needDeleteWhiteBorder 是否去除白边
     * @return Bitmap
     */
    fun createQRCode(
        text: String,
        size: Int = 500,
        logo: Bitmap? = null,
        logoWith: Int = 100,
        logoHigh: Int = 100,
        logoRadiusX: Float = 0f,
        logoRadiusY: Float = 0f,
        strokeWith: Int = 0,
        strokeColor: Int = Color.TRANSPARENT,
        needDeleteWhiteBorder: Boolean = false
    ): Bitmap? {
        return try {
            if (logo == null) {
                createQRCode(text, size, needDeleteWhiteBorder)
            } else {
                createQRCode(text, size, needDeleteWhiteBorder)?.let {
                    addStorkLogo(
                        it,
                        logo,
                        logoWith,
                        logoHigh,
                        logoRadiusX,
                        logoRadiusY,
                        strokeWith.coerceAtMost(logoWith.coerceAtMost(logoHigh)),
                        strokeColor
                    )
                }
            }
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 生成二维码
     * @param text 需要生成二维码的文字、网址等
     * @param size 需要生成二维码的大小, 默认大小为 500
     * @return bitmap
     */
    private fun createQRCode(text: String, size: Int = 500, needDeleteWhiteBorder: Boolean = false): Bitmap? {
        var mSize = size
        return try {
            val hints = Hashtable<EncodeHintType, Any?>()
            hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.MARGIN] = 1
            var bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, mSize, mSize, hints)
            if (needDeleteWhiteBorder) {
                bitMatrix = deleteWhite(bitMatrix)
                mSize = bitMatrix.width
            }
            val pixels = IntArray(mSize * mSize)
            for (y in 0 until mSize) {
                for (x in 0 until mSize) {
                    pixels[y * mSize + x] = if (bitMatrix[x, y]) {
                        Color.BLACK
                    } else {
                        Color.WHITE
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, mSize, 0, 0, mSize, mSize)
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 在二维码中间添加Logo图案(带描边)
     * @param src         原图
     * @param logo        logo
     * @param logoWith     添加logo的宽度
     * @param logoHigh     添加logo的高度
     * @param logoRadiusX  logo圆角
     * @param logoRadiusY  logo圆角
     * @param storkWith    描边宽度
     * @param storkColor   描边颜色
     * @return Bitmap
     */
    private fun addStorkLogo(
        src: Bitmap,
        logo: Bitmap,
        logoWith: Int,
        logoHigh: Int,
        logoRadiusX: Float,
        logoRadiusY: Float,
        storkWith: Int,
        storkColor: Int
    ): Bitmap? {
        // 获取图片的宽高
        val srcWidth = src.width
        val srcHeight = src.height
        val logoW = logo.width
        val logoH = logo.height
        if (srcWidth == 0 || srcHeight == 0) {
            return null
        }
        if (logoW == 0 || logoH == 0) {
            return src
        }
        val scaleW = logoWith / logoW.toFloat()
        val scaleH = logoHigh / logoH.toFloat()
        val matrix = Matrix()
        matrix.postScale(scaleW, scaleH)
        matrix.postTranslate(((srcWidth shr 1) - (logoWith shr 1)).toFloat(), ((srcHeight shr 1) - (logoHigh shr 1)).toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bitmapShader = BitmapShader(logo, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        bitmapShader.setLocalMatrix(matrix)
        var bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, 0f, 0f, null)

            if (storkWith > 0) {
                paint.color = if (storkColor == 0) Color.WHITE else storkColor
                canvas.drawRoundRect(
                    RectF(
                        ((srcWidth shr 1) - (logoWith shr 1) - storkWith).toFloat(),
                        ((srcHeight shr 1) - (logoHigh shr 1) - storkWith).toFloat(),
                        ((srcWidth shr 1) + (logoWith shr 1) + storkWith).toFloat(),
                        ((srcHeight shr 1) + (logoHigh shr 1) + storkWith).toFloat()
                    ), logoRadiusX, logoRadiusY, paint
                )
                paint.shader = bitmapShader
                canvas.drawRoundRect(
                    RectF(
                        ((srcWidth shr 1) - (logoWith shr 1)).toFloat(),
                        ((srcHeight shr 1) - (logoHigh shr 1)).toFloat(),
                        ((srcWidth shr 1) + (logoWith shr 1)).toFloat(),
                        ((srcHeight shr 1) + (logoHigh shr 1)).toFloat()
                    ), logoRadiusX, logoRadiusY, paint
                )
            } else {
                paint.shader = bitmapShader
                canvas.drawRoundRect(
                    RectF(
                        ((srcWidth shr 1) - (logoWith shr 1)).toFloat(),
                        ((srcHeight shr 1) - (logoHigh shr 1)).toFloat(),
                        ((srcWidth shr 1) + (logoWith shr 1)).toFloat(),
                        ((srcHeight shr 1) + (logoHigh shr 1)).toFloat()
                    ), logoRadiusX, logoRadiusY, paint
                )
            }
        } catch (e: Exception) {
            e.stackTrace
            bitmap = null
        }
        return bitmap
    }

    private fun deleteWhite(matrix: BitMatrix): BitMatrix {
        val rec = matrix.enclosingRectangle
        val resWidth = rec[2] + 1
        val resHeight = rec[3] + 1
        val resMatrix = BitMatrix(resWidth, resHeight)
        resMatrix.clear()
        for (i in 0 until resWidth) {
            for (j in 0 until resHeight) {
                if (matrix[i + rec[0], j + rec[1]]) resMatrix[i] = j
            }
        }
        return resMatrix
    }


    /**
     * 生成条形码
     * @param content 要生成条形码包含的内容
     * @param widthPix 条形码的宽度
     * @param heightPix 条形码的高度
     * @param isShowContent  是否显示条形码包含的内容
     * @return 返回生成条形的位图
     */
    fun createBarcode(content: String, widthPix: Int, heightPix: Int, isShowContent: Boolean = true): Bitmap? {
        if (content.isNotEmpty() && !isContainChinese(content)) {
            // 配置参数
            val hints: MutableMap<EncodeHintType, Any?> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            val writer = MultiFormatWriter()
            try {
                // 图像数据转换，使用了矩阵转换 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
                val bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, widthPix, heightPix, hints)
                val pixels = IntArray(widthPix * heightPix)
                for (y in 0 until heightPix) {
                    for (x in 0 until widthPix) {
                        if (bitMatrix[x, y]) {
                            pixels[y * widthPix + x] = -0x1000000
                        } else {
                            pixels[y * widthPix + x] = -0x1
                        }
                    }
                }
                var bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
                if (isShowContent) {
                    bitmap = showContent(bitmap, content)
                }
                return bitmap
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * 显示条形的内容
     * @param bCBitmap 已生成的条形码的位图
     * @param content  条形码包含的内容
     * @return 返回生成的新位图
     */
    private fun showContent(bCBitmap: Bitmap, content: String): Bitmap? {
        if (content.isNotEmpty()) {
            val paint = Paint()
            paint.color = Color.BLACK
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.textSize = 20f
            // 测量字符串的宽度
            val textWidth = paint.measureText(content)
            val fm = paint.fontMetrics
            // 绘制字符串矩形区域的高度
            val textHeight = fm.bottom - fm.top
            // x 轴的缩放比率
            val scaleRateX = bCBitmap.width / textWidth
            paint.textScaleX = scaleRateX
            // 绘制文本的基线
            val baseLine = bCBitmap.height + textHeight
            // 创建一个图层，然后在这个图层上绘制bCBitmap、content
            val bitmap = Bitmap.createBitmap(bCBitmap.width, (bCBitmap.height + 1.5 * textHeight).toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bCBitmap, 0f, 0f, null)
            canvas.drawText(content, ((bCBitmap.width shr 1) - ((textWidth * scaleRateX).toInt() shr 1)).toFloat(), baseLine, paint)
            canvas.save()
            canvas.restore()
            return bitmap
        }
        return null
    }

    /**
     * 字符串是否包含中文
     * @param str 待校验字符串
     * @return true 包含中文字符 false 不包含中文字符
     */
    private fun isContainChinese(str: String): Boolean {
        if (str.isNotEmpty()) {
            val m = Pattern.compile("[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】]").matcher(str)
            return m.find()
        } else {
            throw RuntimeException("sms context is empty!")
        }
    }

    /**
     * 解码 Uri 二维码图片
     */
    fun scanningImage(context: Context, uri: Uri): String? {
        return getBitmapFormUri(context, uri)?.let { scanningImage(it) }
    }
    /**
     * 解码 Bitmap 二维码图片
     */
    fun scanningImage(qrBitmap: Bitmap): String? {
        return try {
            var scanBitmap = qrBitmap
            val hints = Hashtable<DecodeHintType, Any?>()
            // 设置二维码内容的编码
            hints[DecodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8.toString()
    //        hints[DecodeHintType.TRY_HARDER] = true
    //        hints[DecodeHintType.PURE_BARCODE] = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scanBitmap = qrBitmap.copy(Bitmap.Config.RGBA_F16, true)
            }

            val source = love.nuoyan.android.qr.decode.RGBLuminanceSource(scanBitmap)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = QRCodeReader()
            reader.decode(bitmap, hints).text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getBitmapByUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getBitmapFormUri(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        var inputStream2: InputStream? = null
        return try {
            context.contentResolver.openInputStream(uri)?.let {  input ->
                inputStream = input
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                options.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
                BitmapFactory.decodeStream(input, null, options)

                val displayMetrics = context.resources.displayMetrics
                val heightPixels = displayMetrics.heightPixels
                val widthPixels = displayMetrics.widthPixels
                val originalWidth = options.outWidth
                val originalHeight = options.outHeight
                if (originalWidth == -1 || originalHeight == -1) return null

                val a = ceil(originalHeight / heightPixels.toDouble()).toInt()
                val b = ceil(originalWidth / widthPixels.toDouble()).toInt()
                val max = a.coerceAtLeast(b)
                if (max > 1) {
                    options.inSampleSize = max
                }
                options.inJustDecodeBounds = false

                context.contentResolver.openInputStream(uri)?.let { input2 ->
                    inputStream2 = input2
                    BitmapFactory.decodeStream(input2, null, options)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
            inputStream2?.close()
        }
    }
}