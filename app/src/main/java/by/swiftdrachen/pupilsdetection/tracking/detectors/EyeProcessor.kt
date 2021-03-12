package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.configs.EyeProcessorConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Range
import org.opencv.imgproc.Imgproc

class EyeProcessor(
        private val config: EyeProcessorConfig,
        private val eyePreciser: PointDetector,
        private val pupilDetector: PointDetector) {

    private val processingImage = Mat()

    private val hsvChannels: MutableList<Mat> = ArrayList(3)

    private var mutableDetectedEyeCenter = Point()
    private var mutableDetectedPupilCenter = Point()

    var sourceImage: Mat? = null
    var sessionFileManager: SessionFileManager? = null

    val detectedEyeCenter: Point
        get() = mutableDetectedEyeCenter
    val detectedPupilCenter: Point
        get() = mutableDetectedPupilCenter

    fun process() {
        if (sourceImage == null) {
            throw EyeTrackerNotPreparedException("Source image cannot be null")
        }

        val sourceImage = this.sourceImage!!

        val rowsCount = sourceImage.rows()
        val colsCount = sourceImage.cols()

        val topOffset = rowsCount * config.topOffsetPercentage / 100
        val bottomOffset = rowsCount * config.bottomOffsetPercentage / 100

        val rowsRange = Range(topOffset, rowsCount - bottomOffset)
        val colsRange = Range(0, colsCount)

        val cutImage = sourceImage.submat(rowsRange, colsRange)

        Imgproc.cvtColor(cutImage, processingImage, Imgproc.COLOR_RGB2HSV)


        Core.split(processingImage, hsvChannels)

        val hue = hsvChannels[0]
        val saturation = hsvChannels[1]
        val value = hsvChannels[2]

        sessionFileManager?.saveMat(hue, "eye_hue")
        sessionFileManager?.saveMat(saturation, "eye_saturation")
        sessionFileManager?.saveMat(value, "eye_value")

        pupilDetector.processingImage = value
        pupilDetector.detect()
        mutableDetectedPupilCenter = pupilDetector.detectedPoint
        mutableDetectedPupilCenter.y += topOffset

        eyePreciser.processingImage = hue
        eyePreciser.detect()
        mutableDetectedEyeCenter = eyePreciser.detectedPoint
        mutableDetectedEyeCenter.y += topOffset

        hue.release()
        saturation.release()
        value.release()

        hsvChannels.clear()
    }
}
