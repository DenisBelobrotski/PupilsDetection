package by.swiftdrachen.pupilsdetection.tracking.detectors

import android.util.Log
import by.swiftdrachen.pupilsdetection.tracking.configs.PupilContourDetectorConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.DetectorNotPreparedException
import by.swiftdrachen.pupilsdetection.utils.OpenCvUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import kotlin.math.PI
import kotlin.math.abs

class PupilContourDetector(private val config: PupilContourDetectorConfig) : FacePartDetector {
    private var mutableDetectedRects: MutableList<Rect> = mutableListOf()

    private var mutableDetectedImages: MutableList<Mat> = mutableListOf()

    override var targetImage: Mat? = null

    override val detectedRects: List<Rect>
        get() = mutableDetectedRects

    override val detectedImages: List<Mat>
        get() = mutableDetectedImages


    override fun detect() {
        if (targetImage == null) {
            throw DetectorNotPreparedException("target image is null")
        }

        clear()

        val sourceImage = targetImage as Mat
        val processingImage = OpenCvUtils.emptyClone(sourceImage)

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_BGRA2GRAY)
        Imgproc.equalizeHist(processingImage, processingImage)

        val thresholdStartValue = config.thresholdStartValue
        val thresholdStep = config.thresholdStep
        val maxThreshold = config.maxThreshold
        val drawingContourIndex = config.drawingContourIndex
        val contourColor = config.contourColor
        val minSizeRate = config.minSizeRate
        val maxSizeRate = config.maxSizeRate
        val maxAspectRate = config.maxAspectRate
        val maxAreaRate = config.maxAreaRate

        var threshold = thresholdStartValue
        val candidate = Mat()
        val contours = mutableListOf<MatOfPoint>()
        val contoursHierarchy = Mat()
        val logTag = "CONTOURS"

        while (threshold <= maxThreshold) {
            Imgproc.threshold(
                    processingImage, candidate,
                    threshold.toDouble(), maxThreshold.toDouble(),
                    Imgproc.THRESH_BINARY_INV)

            Imgproc.findContours(
                    candidate, contours, contoursHierarchy,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)

            Imgproc.drawContours(candidate, contours, drawingContourIndex, contourColor, Imgproc.FILLED)

            for (contourIndex in contours.indices) {
                val contour = contours[contourIndex]
                val contourArea = Imgproc.contourArea(contour)
                val contourRect = Imgproc.boundingRect(contour)
                val radius = contourRect.width.toDouble() / 2.0

                val sizeRate = contourRect.width.toDouble() / sourceImage.cols().toDouble()
                val aspectRate = abs(1.0 - contourRect.width.toDouble() / contourRect.height.toDouble())
                val areaRate = abs(1.0 - contourArea / (PI * radius * radius))

                val isSizeRateInRange = sizeRate in minSizeRate..maxSizeRate
                val isCorrectAspectRate = aspectRate <= maxAspectRate
                val isCorrectAreaRate = areaRate <= maxAreaRate

                if (isSizeRateInRange && isCorrectAspectRate && isCorrectAreaRate) {
                    mutableDetectedRects.add(contourRect)
                    mutableDetectedImages.add(sourceImage.submat(contourRect))
                }

                Log.d(logTag, "threshold: $threshold\n" +
                        "***contour***\n" +
                        "index: $contourIndex,\n" +
                        "area: $contourArea,\n" +
                        "rect: $contourRect,\n" +
                        "radius: $radius,\n" +
                        "***rates***\n" +
                        "sizeRate: $sizeRate,\n" +
                        "aspectRate: $aspectRate,\n" +
                        "areaRate: $areaRate,\n" +
                        "***rate checks***\n" +
                        "sizeRate: $isSizeRateInRange,\n" +
                        "aspectRate: $isCorrectAspectRate,\n" +
                        "areaRate: $isCorrectAreaRate,\n"
                )
            }

            contours.clear()

            threshold += thresholdStep
        }
    }


    override fun clear() {
        mutableDetectedRects.clear()
        mutableDetectedImages.clear()
    }
}
