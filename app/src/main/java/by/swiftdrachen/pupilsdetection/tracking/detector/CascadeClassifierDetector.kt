package by.swiftdrachen.pupilsdetection.tracking.detector

import by.swiftdrachen.pupilsdetection.tracking.abstraction.ICascadeClassifierConfig
import by.swiftdrachen.pupilsdetection.tracking.abstraction.IRectDetector
import by.swiftdrachen.pupilsdetection.tracking.exception.DetectorNotPreparedException
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.objdetect.CascadeClassifier

class CascadeClassifierDetector(
        private val cascadeClassifier: CascadeClassifier,
        private val config: ICascadeClassifierConfig) : IRectDetector {
    private val mutableDetectedRects: MutableList<Rect> = mutableListOf()

    override var processingImage: Mat? = null

    override val detectedRects: List<Rect>
        get() = mutableDetectedRects

    override fun detect() {
        if (processingImage == null) {
            throw DetectorNotPreparedException("processing image is null")
        }

        clear()

        val sourceImage = processingImage as Mat

        val facesRectMat = MatOfRect()

        val minSize = sourceImage.size()
        minSize.width *= config.minSizeRatio
        minSize.height *= config.minSizeRatio

        val maxSize = sourceImage.size()
        maxSize.width *= config.maxSizeRatio
        maxSize.height *= config.maxSizeRatio

        cascadeClassifier.detectMultiScale(
                sourceImage, facesRectMat,
                config.scaleFactor, config.minNeighbours, config.flags,
                minSize, maxSize)

        // TODO: Optimize toList call because it allocates memory. Try to enumerate facesRectMat
        // TODO: Release facesRectMat
        mutableDetectedRects.addAll(facesRectMat.toList())
    }


    override fun clear() {
        mutableDetectedRects.clear()
    }
}
