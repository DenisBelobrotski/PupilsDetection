package by.swiftdrachen.pupilsdetection.tracking

import android.util.Log
import by.swiftdrachen.pupilsdetection.tracking.exceptions.DetectorNotPreparedException
import by.swiftdrachen.pupilsdetection.utils.OpenCvUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.PI
import kotlin.math.abs

class ContourPupilDetector {
    private var detectedRects: MutableList<Rect> = mutableListOf()

    private var detectedImages: MutableList<Mat> = mutableListOf()

    var targetImage: Mat? = null

    val DetectedRects: List<Rect>
        get() = detectedRects

    val DetectedImages: List<Mat>
        get() = detectedImages


    fun detect() {
        if (targetImage == null) {
            throw DetectorNotPreparedException("target image is null")
        }

        clear()

        val sourceImage = targetImage as Mat
        val processingImage = OpenCvUtils.emptyClone(sourceImage)

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_BGRA2GRAY)
        Imgproc.equalizeHist(processingImage, processingImage)

        var threshold = 0
        val maxThreshold = 255
        val candidate = Mat()
        val contours = mutableListOf<MatOfPoint>()
        val contoursHierarchy = Mat()
        val drawingContourIndex = -1 //all contours
        val contourColor = Scalar(255.0, 255.0, 255.0)
        val minSizeRate = 0.2 //0.25
        val maxSizeRate = 0.75 //0.41
        val maxAspectRate = 0.25 //0.2
        val maxAreaRate = 0.2
        val logTag = "CONTOURS"

        while (threshold < maxThreshold + 1) {
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
                    detectedRects.add(contourRect)
                    detectedImages.add(sourceImage.submat(contourRect))
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

            threshold += 1
        }
    }

    fun clear() {
        detectedRects.clear()
        detectedImages.clear()
    }
}