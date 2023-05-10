package love.nuoyan.android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.File

abstract class UtilsPictures {
    private var imageSaveUri: Uri? = null
    private lateinit var resultCameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var resultCameraPermission: ActivityResultLauncher<String>
    private lateinit var resultPicturesLauncher: ActivityResultLauncher<Intent>
    private lateinit var resultPicturesPermission: ActivityResultLauncher<String>

    abstract fun onCamera(permission: Boolean, code: Int, uri: Uri?)
    abstract fun onPictures(permission: Boolean, code: Int, uri: Uri?)

    fun openCamera() {
        resultCameraPermission.launch(Manifest.permission.CAMERA)
    }
    fun registerCamera(activity: AppCompatActivity) {
        resultCameraLauncher = activity.registerForActivityResult(TakePicture()) {
            if (it == Activity.RESULT_OK) {
                onCamera(true, it, imageSaveUri)
            } else {
                onCamera(true, it, null)
            }
        }
        resultCameraPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                launchCamera()              // 用户同意了该权限
            } else {
                onCamera(false, 0, null)    // 用户拒绝了该权限
            }
        }
    }
    fun registerCamera(fragment: Fragment) {
        resultCameraLauncher = fragment.registerForActivityResult(TakePicture()) {
            if (it == Activity.RESULT_OK) {
                onCamera(true, it, imageSaveUri)
            } else {
                onCamera(true, it, null)
            }
        }
        resultCameraPermission = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                launchCamera()              // 用户同意了该权限
            } else {
                onCamera(false, 0, null)    // 用户拒绝了该权限
            }
        }
    }
    private fun launchCamera() {
        val imageSaveFile = File(UtilsImage.imageDir, "save_image_${System.currentTimeMillis()}.jpg")
        imageSaveUri = UtilsFile.getUri(imageSaveFile.absolutePath)
        resultCameraLauncher.launch(imageSaveUri)
    }

    fun openPictures() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            resultPicturesPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            resultPicturesPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    fun registerPictures(activity: AppCompatActivity) {
        resultPicturesLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> onPictures(true, it.resultCode, it.data?.data)
                Activity.RESULT_CANCELED -> onPictures(true, it.resultCode, null)
                else -> onPictures(true, it.resultCode, null)
            }
        }
        resultPicturesPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                launchPictures()            // 用户同意了该权限
            } else {
                onPictures(false, 0, null)  // 用户拒绝了该权限
            }
        }
    }
    fun registerPictures(fragment: Fragment) {
        resultPicturesLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> onPictures(true, it.resultCode, it.data?.data)
                Activity.RESULT_CANCELED -> onPictures(true, it.resultCode, null)
                else -> onPictures(true, it.resultCode, null)
            }
        }
        resultPicturesPermission = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                launchPictures()        // 用户同意了该权限
            } else {
                onPictures(false, 0, null) // 用户拒绝了该权限
            }
        }
    }
    private fun launchPictures() {
        val picIntent = Intent(Intent.ACTION_PICK)
        picIntent.type = "image/*"
        resultPicturesLauncher.launch(picIntent)
    }
}

class TakePicture : ActivityResultContract<Uri, Int>() {
    @CallSuper
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
    }

    override fun getSynchronousResult(
        context: Context,
        input: Uri
    ): SynchronousResult<Int>? {
        return null
    }

    // Activity.RESULT_OK   Activity.RESULT_CANCELED
    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return resultCode
    }
}