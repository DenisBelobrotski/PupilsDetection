package by.swiftdrachen.pupilsdetection.tracking.detectors

import org.opencv.core.Mat
import org.opencv.core.Point

interface IPointDetector {
    var processingImage: Mat?
    val detectedPoint: Point

    fun detect()

    //TODO: release mats in implementations
    fun clear()
}
