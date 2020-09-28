package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.utils.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.configs.CascadeClassifierConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.DetectorNotPreparedException
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class CascadeClassifierDetector(
        private val cascadeClassifier: CascadeClassifier,
        private val config: CascadeClassifierConfig) : FacePartDetector {
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

        val facesRectMat = MatOfRect()

        val minSize = sourceImage.size()
        minSize.width *= config.minSizeRatio
        minSize.height *= config.minSizeRatio

        val maxSize = sourceImage.size()
        maxSize.width *= config.maxSizeRatio
        maxSize.height *= config.maxSizeRatio

        //TODO: move this out to optimize
        val grayscale = OpenCvUtils.emptyClone(sourceImage)
        Imgproc.cvtColor(sourceImage, grayscale, Imgproc.COLOR_BGRA2GRAY)

        //TODO: move this out to optimize
        Imgproc.equalizeHist(grayscale, grayscale)

        cascadeClassifier.detectMultiScale(
                sourceImage, facesRectMat,
                config.scaleFactor, config.minNeighbours, config.flags,
                minSize, maxSize)

        mutableDetectedRects.addAll(facesRectMat.toList())
        mutableDetectedRects.forEach {rect ->
            mutableDetectedImages.add(sourceImage.submat(rect))
        }
    }


    override fun clear() {
        mutableDetectedRects.clear()
        mutableDetectedImages.clear()
    }
}
