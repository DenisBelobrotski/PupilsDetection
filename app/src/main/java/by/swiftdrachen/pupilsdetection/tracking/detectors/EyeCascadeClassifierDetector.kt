package by.swiftdrachen.pupilsdetection.tracking.detectors

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect

class EyeCascadeClassifierDetector(
        private val cascadeClassifierDetector: CascadeClassifierDetector) : FacePartDetector {
    private var mutableDetectedRects: MutableList<Rect> = mutableListOf()
    private var mutableDetectedImages: MutableList<Mat> = mutableListOf()

    override var targetImage: Mat?
        get() = cascadeClassifierDetector.targetImage
        set(value) { cascadeClassifierDetector.targetImage = value }

    override val detectedRects: List<Rect>
        get() = mutableDetectedRects

    override val detectedImages: List<Mat>
        get() = mutableDetectedImages


    override fun detect() {
        cascadeClassifierDetector.detect()

        val sourceImage = targetImage!!
        val sourceImageHeight = sourceImage.height().toDouble()

        cascadeClassifierDetector.detectedRects.forEach {rect ->
            val eyeCenter = Point(
                    rect.x.toDouble() + rect.width.toDouble() * 0.5,
                    rect.y.toDouble() + rect.height.toDouble() * 0.5)
            val eyeSize = Point(rect.width.toDouble(), rect.height.toDouble())

            if (eyeCenter.y + eyeSize.y * 0.5 <= sourceImageHeight * 0.6) {
                mutableDetectedRects.add(rect)
            }
        }

        mutableDetectedRects.forEach {rect ->
            mutableDetectedImages.add(sourceImage.submat(rect))
        }
    }

    override fun clear() {
        cascadeClassifierDetector.clear()
        mutableDetectedRects.clear()
        mutableDetectedImages.clear()
    }
}
