package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.configs.ScleraDetectorConfig
import org.opencv.core.Mat
import org.opencv.core.Point

class ScleraDetector(private val config: ScleraDetectorConfig) : PointDetector {
    private val mutableDetectedPoint = Point()

    override var processingImage: Mat? = null

    override val detectedPoint: Point
        get() = mutableDetectedPoint

    override fun detect() {

    }

    override fun clear() {

    }
}
