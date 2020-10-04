package by.swiftdrachen.pupilsdetection.tracking.detectors

import org.opencv.core.Mat
import org.opencv.core.Rect

class FaceCascadeClassifierDetector(
        private val cascadeClassifierDetector: CascadeClassifierDetector) : FacePartDetector {
    override var targetImage: Mat?
        get() = cascadeClassifierDetector.targetImage
        set(value) { cascadeClassifierDetector.targetImage = value }

    override val detectedRects: List<Rect>
        get() = cascadeClassifierDetector.detectedRects

    override val detectedImages: List<Mat>
        get() = cascadeClassifierDetector.detectedImages

    override fun detect() {
        cascadeClassifierDetector.detect()
    }

    override fun clear() {
        cascadeClassifierDetector.clear()
    }
}
