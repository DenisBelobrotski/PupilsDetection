package by.swiftdrachen.pupilsdetection.tracking.detectors

import org.opencv.core.Mat
import org.opencv.core.Rect

interface RectDetector {
    var processingImage: Mat?
    val detectedRects: List<Rect>

    fun detect()

    //TODO: release mats in implementations
    fun clear()
}
