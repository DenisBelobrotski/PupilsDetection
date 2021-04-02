package by.swiftdrachen.eye_tracking_library.abstraction

import org.opencv.core.Mat
import org.opencv.core.Rect

interface IRectDetector : IClearable {
    var processingImage: Mat?
    val detectedRects: List<Rect>

    fun detect()
}
