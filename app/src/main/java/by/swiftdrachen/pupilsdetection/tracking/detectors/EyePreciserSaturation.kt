package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.algorithm.getMassCenter8UC1
import by.swiftdrachen.pupilsdetection.tracking.configs.EyePreciserSaturationConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class EyePreciserSaturation(private val config: EyePreciserSaturationConfig) : IPointDetector {
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
        sessionFileManager?.addLog("EyePreciserSaturation - detection started")

        if (processingImage == null) {
            throw EyeTrackerNotPreparedException("processingImage not set")
        }

        val processingImage = this.processingImage!!

        sessionFileManager?.saveMat(processingImage, "eye_preciser_source", true)
        sessionFileManager?.addLog("EyePreciserSaturation - source saved", true)

        if (config.shouldEqualizeHistogram) {
            Imgproc.equalizeHist(processingImage, processingImage)
            sessionFileManager?.addLog("EyePreciserSaturation - hist equalized")

            sessionFileManager?.saveMat(processingImage, "eye_preciser_equalize_hist", true)
            sessionFileManager?.addLog("EyePreciserSaturation - equalize hist saved", true)
        }

        Imgproc.threshold(
                processingImage, processingImage,
                config.threshold.toDouble(), config.maxThreshold.toDouble(),
                Imgproc.THRESH_BINARY_INV)
        sessionFileManager?.addLog("EyePreciserSaturation - threshold")

        sessionFileManager?.saveMat(processingImage, "eye_preciser_threshold", true)
        sessionFileManager?.addLog("EyePreciserSaturation - threshold", true)

        if (config.isErosionEnabled) {
            Imgproc.erode(
                    processingImage, processingImage,
                    erosionKernel, erosionAnchor, config.erosionIterationsCount)
            sessionFileManager?.addLog("EyePreciserSaturation - erode")

            sessionFileManager?.saveMat(processingImage, "eye_preciser_erode", true)
            sessionFileManager?.addLog("EyePreciserSaturation - erode", true)
        }

        if (config.isDilationEnabled) {
            Imgproc.dilate(
                    processingImage, processingImage,
                    dilationKernel, dilationAnchor, config.dilationIterationsCount)
            sessionFileManager?.addLog("EyePreciserSaturation - dilate")

            sessionFileManager?.saveMat(processingImage, "eye_preciser_dilate", true)
            sessionFileManager?.addLog("EyePreciserSaturation - dilate", true)
        }

        mutableDetectedPoint = getMassCenter8UC1(processingImage)
        sessionFileManager?.addLog("EyePreciserSaturation - center of mass done")

        sessionFileManager?.addLog("EyePreciserSaturation - detection done")
    }

    override fun clear() {
        erosionKernel.release()
        dilationKernel.release()
    }
}
