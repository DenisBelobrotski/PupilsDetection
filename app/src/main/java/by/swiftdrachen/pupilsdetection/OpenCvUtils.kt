package by.swiftdrachen.pupilsdetection

import android.content.Context
import android.net.Uri
import org.opencv.objdetect.CascadeClassifier
import org.opencv.osgi.OpenCVNativeLoader
import java.io.File
import java.io.IOException

const val ShouldShowCachedCascadesForUser = true

class OpenCvUtils {
    companion object {
        fun loadLibraries() {
            val loader = OpenCVNativeLoader()
            loader.init()
        }

        fun loadCascadeFromAssets(context: Context, assetPath: String): CascadeClassifier? {
            val faceCascadeAssetUri = Uri.parse(assetPath)
            val cachedFaceCascadeFile = FileSystemUtils.cacheAssetFile(context, faceCascadeAssetUri,
                    false, ShouldShowCachedCascadesForUser)
            var loadedCascade: CascadeClassifier? = null
            cachedFaceCascadeFile?.let {
                loadedCascade = loadCascade(cachedFaceCascadeFile)
            }

            return loadedCascade
        }

        fun loadCascade(cascadeFile: File): CascadeClassifier {
            val faceCascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
            val isEmpty = faceCascadeClassifier.empty()

            if (isEmpty) {
                throw IOException("Cascade classifier is empty")
            }

            return faceCascadeClassifier
        }
    }
}
