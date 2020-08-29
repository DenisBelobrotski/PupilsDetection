package by.swiftdrachen.pupilsdetection

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val VIDEO_FILE_CHOOSER_REQUEST_CODE = 322

class FileChooser(
    private val targetActivity: Activity,
    private val baseMimeType: String,
    private val concreteMimeType: String) {

    private val lastChosenFileUriMutable: MutableLiveData<Uri> by lazy { MutableLiveData<Uri>() }
    val lastChosenFileUri: LiveData<Uri>
        get() = lastChosenFileUriMutable

    fun choose() {
        val chooserTip = targetActivity.resources.getString(R.string.choose_video_tip)

        var chooserIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooserIntent.type = "${baseMimeType}/${concreteMimeType}"
        chooserIntent.addCategory(Intent.CATEGORY_OPENABLE)
        chooserIntent = Intent.createChooser(chooserIntent, chooserTip)

        try {
            targetActivity.startActivityForResult(chooserIntent, VIDEO_FILE_CHOOSER_REQUEST_CODE)
        } catch (exception: ActivityNotFoundException) {
            val message = targetActivity.resources.getString(R.string.file_manager_error)
            Toast.makeText(targetActivity, message, Toast.LENGTH_SHORT).show();
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == VIDEO_FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val fileUri = intent?.data
            fileUri?.let {
                lastChosenFileUriMutable.value = fileUri
                Toast.makeText(targetActivity, fileUri.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
