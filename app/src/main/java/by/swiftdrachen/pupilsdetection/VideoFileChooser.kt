package by.swiftdrachen.pupilsdetection

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
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
            val filePath = intent?.data?.path
            filePath?.let {
                lastChosenFileMutable.value = File(filePath)
                Toast.makeText(targetActivity, filePath, Toast.LENGTH_LONG).show();
            }

            val uri = intent?.data
            uri?.let {
                val fileName = getFileName(uri)
                fileName?.let {
                    lastChosenFileMutable.value = File(fileName)
                    Toast.makeText(targetActivity, fileName, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        // Obtain a cursor with information regarding this uri
        val cursor = targetActivity.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (cursor.count <= 0) {
                cursor.close()
                throw IllegalArgumentException("Can't obtain file name, cursor is empty")
            }
            cursor.moveToFirst()
            val fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            cursor.close()

            return fileName
        }

        return null
    }
}
