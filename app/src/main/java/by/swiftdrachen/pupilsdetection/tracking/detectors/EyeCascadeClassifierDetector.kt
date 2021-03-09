package by.swiftdrachen.pupilsdetection.tracking.detectors

import org.opencv.core.Mat
import org.opencv.core.Rect

class EyeCascadeClassifierDetector(
        private val cascadeClassifierDetector: CascadeClassifierDetector) : RectDetector {
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
