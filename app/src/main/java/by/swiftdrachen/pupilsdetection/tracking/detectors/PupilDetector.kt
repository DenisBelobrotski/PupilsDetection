package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.configs.PupilDetectorConfig
import org.opencv.core.Mat
import org.opencv.core.Point

class PupilDetector(private val config: PupilDetectorConfig) : PointDetector {
    private val mutableDetectedPoint = Point()

    override var processingImage: Mat? = null

    override val detectedPoint: Point
        get() = mutableDetectedPoint

    override fun detect() {

    }

    override fun clear() {

    }
}
