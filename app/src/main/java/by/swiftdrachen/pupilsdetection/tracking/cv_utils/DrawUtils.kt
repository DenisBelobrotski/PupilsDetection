package by.swiftdrachen.pupilsdetection.tracking.cv_utils

import org.opencv.core.Mat
import kotlin.math.max
import kotlin.math.min

class DrawUtils {
    companion object {
        fun getMarkerSizeForMat(mat: Mat, delimeter: Int, minValue: Int): Int {
            return max(min(mat.cols(), mat.rows()) / delimeter, minValue)
        }

        fun getLineThicknessForMat(mat: Mat, delimeter: Int, minValue: Int): Int {
            return max(min(mat.cols(), mat.rows()) / delimeter, minValue)
        }
    }
}
