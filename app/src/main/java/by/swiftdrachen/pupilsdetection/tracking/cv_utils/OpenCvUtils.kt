package by.swiftdrachen.pupilsdetection.tracking.cv_utils

import android.content.Context
import android.net.Uri
import by.swiftdrachen.pupilsdetection.utils.FileSystemUtils
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.objdetect.CascadeClassifier
import org.opencv.osgi.OpenCVNativeLoader
import java.io.File
import java.io.IOException

class OpenCvUtils {
    companion object {
        private const val ShouldShowCachedCascadesForUser = true

        fun loadLibraries() {
            val loader = OpenCVNativeLoader()
            loader.init()
        }

        fun loadCascadeFromAssets(context: Context, assetPath: String): CascadeClassifier? {
            val faceCascadeAssetUri = Uri.parse(assetPath)
            val cachedFaceCascadeFile =
                FileSystemUtils.cacheAssetFile(context, faceCascadeAssetUri,
                        false, ShouldShowCachedCascadesForUser)
            var loadedCascade: CascadeClassifier? = null
            cachedFaceCascadeFile?.let {
                loadedCascade =
                    loadCascade(cachedFaceCascadeFile)
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

        fun loadUserMat(context: Context, uri: Uri): Mat? {
            var resultMat: Mat? = null

            val resultBitmap =
                FileSystemUtils.loadUserBitmap(context, uri)
            resultBitmap?.let {
                resultMat = Mat()
                Utils.bitmapToMat(resultBitmap, resultMat)
            }

            return resultMat
        }

        fun emptyClone(sourceMat: Mat): Mat {
            return Mat(sourceMat.rows(), sourceMat.cols(), sourceMat.type())
        }

        fun getMatCenter(mat: Mat): Point {
            return Point((mat.cols() / 2).toDouble(), (mat.rows() / 2).toDouble())
        }
    }
}
