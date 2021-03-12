package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.algorithm.getMassCenter8UC1
import by.swiftdrachen.pupilsdetection.tracking.configs.EyePreciserHueConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class EyePreciserHue(private val config: EyePreciserHueConfig) : PointDetector {
    private val erosionKernel = Mat()
    private val erosionAnchor = Point(-1.0, -1.0)
    private val dilationKernel = Mat()
    private val dilationAnchor = Point(-1.0, -1.0)

    private var mutableDetectedPoint = Point()

    override var processingImage: Mat? = null

    override val detectedPoint: Point
        get() = mutableDetectedPoint

    var sessionFileManager: SessionFileManager? = null

    override fun detect() {
        if (processingImage == null) {
            throw EyeTrackerNotPreparedException("processingImage not set")
        }

        val processingImage = this.processingImage!!

        Imgproc.threshold(
                processingImage, processingImage,
                config.threshold.toDouble(), config.maxThreshold.toDouble(),
                Imgproc.THRESH_BINARY)
        sessionFileManager?.saveMat(processingImage, "eye_pupil_threshold")

        if (config.isErosionEnabled) {
            Imgproc.erode(
                    processingImage, processingImage,
                    erosionKernel, erosionAnchor, config.erosionIterationsCount)
            sessionFileManager?.saveMat(processingImage, "eye_pupil_erode")
        }

        if (config.isDilationEnabled) {
            Imgproc.dilate(
                    processingImage, processingImage,
                    dilationKernel, dilationAnchor, config.dilationIterationsCount)
            sessionFileManager?.saveMat(processingImage, "eye_pupil_dilate")
        }

        mutableDetectedPoint = getMassCenter8UC1(processingImage)
    }

    override fun clear() {
        erosionKernel.release()
        dilationKernel.release()
    }
}
