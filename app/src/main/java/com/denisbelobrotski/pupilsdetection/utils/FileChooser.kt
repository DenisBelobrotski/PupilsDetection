package com.denisbelobrotski.pupilsdetection.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denisbelobrotski.pupilsdetection.R

class FileChooser(
    private val targetActivity: Activity,
    private val baseMimeType: String,
    private val concreteMimeType: String) {

    private val lastChosenFileUriMutable: MutableLiveData<Uri> by lazy { MutableLiveData<Uri>() }
    val lastChosenFileUri: LiveData<Uri>
        get() = lastChosenFileUriMutable

    fun choose() {
        val chooserTip = targetActivity.resources.getString(R.string.choose_file_tip)

        var chooserIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooserIntent.type = "${baseMimeType}/${concreteMimeType}"
        chooserIntent.addCategory(Intent.CATEGORY_OPENABLE)
        chooserIntent = Intent.createChooser(chooserIntent, chooserTip)

        try {
            targetActivity.startActivityForResult(chooserIntent, VideoFileChooserRequestCode)
        } catch (exception: ActivityNotFoundException) {
            val message = targetActivity.resources.getString(R.string.file_manager_error)
            Toast.makeText(targetActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == VideoFileChooserRequestCode && resultCode == RESULT_OK) {
            val fileUri = intent?.data
            fileUri?.let {
                lastChosenFileUriMutable.value = fileUri
                Toast.makeText(targetActivity, fileUri.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val VideoFileChooserRequestCode = 322
    }
}
