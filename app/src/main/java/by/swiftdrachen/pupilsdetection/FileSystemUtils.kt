package by.swiftdrachen.pupilsdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileSystemUtils {
    companion object {
        fun loadBitmapResource(context: Context, assetPath: String): Bitmap {
            val assetInputStream = context.assets.open(assetPath)
            var resultBitmap = BitmapFactory.decodeStream(assetInputStream)
            assetInputStream.close()
            val bitmapConfig = resultBitmap.config

            if (bitmapConfig != Bitmap.Config.ARGB_8888 && bitmapConfig != Bitmap.Config.RGB_565) {
                resultBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, false)
            }

            return resultBitmap
        }

        fun writeCacheFile(context: Context, inputStream: InputStream, outputFileName: String, rewrite: Boolean = false): File {
            val cachedFile = File(context.cacheDir, outputFileName)

            if (cachedFile.exists() && rewrite) {
                cachedFile.delete()
            }
            cachedFile.createNewFile()

            val outputStream = FileOutputStream(cachedFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            outputStream.close()

            return cachedFile
        }

        fun cacheAssetFile(context: Context, assetUri: Uri, rewrite: Boolean = false): File? {
            val assetPath = assetUri.path
            val assetFileName = assetUri.lastPathSegment
            var cachedFile: File? = null

            assetPath?.let {
                assetFileName?.let {
                    val inputStream = context.assets.open(assetPath)

                    cachedFile = writeCacheFile(context, inputStream, assetFileName, rewrite)

                    inputStream.close()
                }
            }

            return cachedFile
        }

        fun cacheUserFile(context: Context, userFileUri: Uri, rewrite: Boolean = false): File? {
            var cachedFile: File? = null
            val userFileName = getUserFileName(context, userFileUri)
            val userFileInputStream = openUserFileInputStream(context, userFileUri)

            if (userFileName != null && userFileInputStream != null) {
                cachedFile = writeCacheFile(context, userFileInputStream, userFileName, rewrite)
            }

            userFileInputStream?.close()

            return cachedFile
        }

        fun openUserFileInputStream(context: Context, userFileUri: Uri): InputStream? {
            return context.contentResolver.openInputStream(userFileUri)
        }

        fun getUserFileName(context: Context, uri: Uri): String? {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
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
}
