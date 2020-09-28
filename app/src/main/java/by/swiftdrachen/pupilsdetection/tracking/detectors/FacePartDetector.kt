package by.swiftdrachen.pupilsdetection.tracking.detectors

import org.opencv.core.Mat
import org.opencv.core.Rect

interface FacePartDetector {
    var targetImage: Mat?
    val detectedRects: List<Rect>
    val detectedImages: List<Mat>

    fun detect()
    fun clear()
}
