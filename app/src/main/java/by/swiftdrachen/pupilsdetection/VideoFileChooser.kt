package by.swiftdrachen.pupilsdetection

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

private const val VIDEO_FILE_CHOOSER_REQUEST_CODE = 322
private const val CHOOSER_FILE_FILTER = "video/*"

class VideoFileChooser(private val targetActivity: Activity) {
    private val lastChosenFileMutable: MutableLiveData<File> by lazy { MutableLiveData<File>() }
    val lastChosenFile: LiveData<File>
        get() = lastChosenFileMutable

    fun choose() {
        val chooserTip = targetActivity.resources.getString(R.string.choose_video_tip)

        var chooserIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooserIntent.type = CHOOSER_FILE_FILTER
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
                lastChosenFileMutable.value = FileSystemUtils.cacheUserFile(targetActivity, fileUri, true)

                Toast.makeText(targetActivity, fileUri.toString(), Toast.LENGTH_LONG).show();
                lastChosenFileMutable.value?.let {
                    Toast.makeText(targetActivity, it.absolutePath, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
