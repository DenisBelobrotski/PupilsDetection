package by.swiftdrachen.pupilsdetection.tracking.abstraction

import org.opencv.core.Mat
import org.opencv.core.Rect

interface IRectDetector : IClearable {
    var processingImage: Mat?
    val detectedRects: List<Rect>

    fun detect()
}
