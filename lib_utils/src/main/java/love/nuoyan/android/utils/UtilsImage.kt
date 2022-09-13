package love.nuoyan.android.utils

import android.content.ContentUris
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.File
import java.io.FileOutputStream

object UtilsImage {
    /** 默认图片缓存目录 */
    val imageDir: File
        get() {
            val file = File(UtilsFile.getAppCacheDir(), "pictures")
            // 图片缓存目录创建
            try {
                if (file.exists()) {
                    if (file.isFile) {
                        file.delete()
                        file.mkdir()
                    }
                } else {
                    file.mkdir()
                }
            } catch (e: Exception) {
                UtilsLog.logE("UtilsImage Init ## $e")
            }
            return file
        }

    suspend fun clearDiskCache() = withContext(IO) {
        Glide.get(Utils.appContext).clearDiskCache()
    }

    fun loadImage(url: String, signature: String = ""): Drawable? {
        return try {
            Glide.with(Utils.appContext).load(url)
                .signature(ObjectKey(signature))
                .submit()
                .get()
        } catch (e: Exception) {
            UtilsLog.log(e.stackTraceToString(), "UtilsImage")
            null
        }
    }

    fun loadBitmap(url: String, signature: String = ""): Bitmap? {
        return try {
            Glide.with(Utils.appContext).asBitmap().load(url)
                .signature(ObjectKey(signature))
                .submit()
                .get()
        } catch (e: Exception) {
            UtilsLog.log(e.stackTraceToString(), "UtilsImage")
            null
        }
    }

    fun loadBitmap(url: String, roundedCorners: Int, signature: String = ""): Bitmap? {
        return try {
            Glide.with(Utils.appContext).asBitmap()
                .apply(
                    RequestOptions.bitmapTransform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(roundedCorners.dp2px.toInt())
                        )
                    ))
                .load(url)
                .signature(ObjectKey(signature))
                .submit()
                .get()
        } catch (e: Exception) {
            UtilsLog.log(e.stackTraceToString(), "UtilsImage")
            null
        }
    }

    fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            drawableToBitmap(drawable)
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    /** 通过uri加载图片 */
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        return try {
            parcelFileDescriptor = Utils.appContext.contentResolver.openFileDescriptor(uri, "r")
            BitmapFactory.decodeFileDescriptor(parcelFileDescriptor?.fileDescriptor)
        } catch (e: Exception) {
            UtilsLog.logW("getBitmapFromUri ## ${e.stackTraceToString()}")
            null
        } finally {
            parcelFileDescriptor?.close()
        }
    }

    /** 读取图片角度 */
    fun readImageDegree(imagePath: String): Float {
        return try {
            val exifInterface = ExifInterface(imagePath)
            var degree = 0f
            when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270f
            }
            degree
        } catch (e: Exception) {
            UtilsLog.logW(e.stackTraceToString())
            0f
        }
    }

    /** 旋转位图 */
    fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        return if (degree == 0f) {
            bitmap
        } else {
            val matrix = Matrix()       // 旋转图片 动作
            matrix.postRotate(degree)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    fun saveBitmap(bitmap: Bitmap): File? {
        val imageSaveFile = File(imageDir, "save_image_${System.currentTimeMillis()}.jpg")
        val fileOutputStream = FileOutputStream(imageSaveFile)
        return try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream)
            fileOutputStream.flush()
            imageSaveFile
        } catch (e: Exception) {
            imageSaveFile.delete()
            UtilsLog.logW("saveBitmap ## ${e.stackTraceToString()}")
            null
        } finally {
            try {
                fileOutputStream.close()
            } catch (e: Exception) {
                UtilsLog.logW("saveBitmap ## $e")
            }
        }
    }

    fun readPicture(uri: Uri): Bitmap? {
        return try {
            when {
                DocumentsContract.isDocumentUri(Utils.appContext, uri) -> {         // 如果是 document 类型的 Uri，则通过 document id 处理
                    val docId = DocumentsContract.getDocumentId(uri)
                    when (uri.authority) {
                        "com.android.providers.media.documents" -> {
                            val id = docId.split(":")[1]
                            val selection = MediaStore.Images.Media._ID + "=" + id  // 解析出数字格式的 id
                            getBitmapFromUri(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection))
                        }
                        "com.android.providers.downloads.documents" -> {
                            val contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), docId.toLong())
                            getBitmapFromUri(contentUri)
                        }
                        else -> null
                    }
                }
                "content".equals(uri.scheme, true) -> {                             // 如果是content类型的Uri，则使用普通方式处理
                    getBitmapFromUri(uri)
                }
                "file".equals(uri.scheme, true) -> {
                    getBitmapFromUri(uri)
                }
                else -> null
            }
        } catch (e: Exception) {
            UtilsLog.logW("readPicture ## ${e.stackTraceToString()}")
            null
        }
    }
}