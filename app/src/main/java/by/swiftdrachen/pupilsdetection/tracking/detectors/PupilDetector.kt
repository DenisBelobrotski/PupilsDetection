package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.algorithm.getMassCenter8UC1
import by.swiftdrachen.pupilsdetection.tracking.configs.PupilDetectorConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class PupilDetector(private val config: PupilDetectorConfig) : PointDetector {
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
        sessionFileManager?.addLog("PupilDetector - detection started")

        if (processingImage == null) {
            throw EyeTrackerNotPreparedException("processingImage not set")
        }

        val processingImage = this.processingImage!!

        if (config.shouldEqualizeHistogram) {
            Imgproc.equalizeHist(processingImage, processingImage)
            sessionFileManager?.addLog("PupilDetector - equalize hist done")

            sessionFileManager?.saveMat(processingImage, "eye_pupil_equalize_hist", true)
            sessionFileManager?.addLog("PupilDetector - equalize hist saved", true)
        }

        Imgproc.threshold(
                processingImage, processingImage,
                config.threshold.toDouble(), config.maxThreshold.toDouble(),
                Imgproc.THRESH_BINARY_INV)
        sessionFileManager?.addLog("PupilDetector - threshold done")

        sessionFileManager?.saveMat(processingImage, "eye_pupil_threshold", true)
        sessionFileManager?.addLog("PupilDetector - threshold saved", true)

        if (config.isErosionEnabled) {
            Imgproc.erode(
                    processingImage, processingImage,
                    erosionKernel, erosionAnchor, config.erosionIterationsCount)
            sessionFileManager?.addLog("PupilDetector - erode done")

            sessionFileManager?.saveMat(processingImage, "eye_pupil_erode", true)
            sessionFileManager?.addLog("PupilDetector - erode saved", true)
        }

        if (config.isDilationEnabled) {
            Imgproc.dilate(
                    processingImage, processingImage,
                    dilationKernel, dilationAnchor, config.dilationIterationsCount)
            sessionFileManager?.addLog("PupilDetector - dilate done")

            sessionFileManager?.saveMat(processingImage, "eye_pupil_dilate", true)
            sessionFileManager?.addLog("PupilDetector - dilate saved", true)
        }

        mutableDetectedPoint = getMassCenter8UC1(processingImage)
        sessionFileManager?.addLog("PupilDetector - center of mass done")

        sessionFileManager?.addLog("PupilDetector - detection done")
    }

    override fun clear() {
        erosionKernel.release()
        dilationKernel.release()
    }
}
