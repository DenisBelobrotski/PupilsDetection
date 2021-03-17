package by.swiftdrachen.pupilsdetection.tracking.detector

import by.swiftdrachen.pupilsdetection.tracking.abstraction.IRectDetector
import org.opencv.core.Mat
import org.opencv.core.Rect

class EyeCascadeClassifierDetector(
        private val cascadeClassifierDetector: CascadeClassifierDetector) : IRectDetector {
    override var processingImage: Mat?
        get() = cascadeClassifierDetector.processingImage
        set(value) { cascadeClassifierDetector.processingImage = value }

    override val detectedRects: List<Rect>
        get() = cascadeClassifierDetector.detectedRects

    override fun detect() {
        cascadeClassifierDetector.detect()
    }

    override fun clear() {
        cascadeClassifierDetector.clear()
    }
}
