package by.swiftdrachen.pupilsdetection.tracking.detectors

import android.util.Log
import org.opencv.core.Mat
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

        cascadeClassifierDetector.detectedRects.forEach {rect ->
            val roiHeight = sourceImage.height().toDouble()
            val y = rect.y.toDouble()
            val height = rect.height.toDouble()

            Log.d("PIDOR", "roiHeight: ${roiHeight}, y: $y, height: $height")

            if (y + height * 0.5 <= roiHeight * 0.6) {
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
