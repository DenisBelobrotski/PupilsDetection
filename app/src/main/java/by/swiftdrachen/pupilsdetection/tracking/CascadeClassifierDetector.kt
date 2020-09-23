package by.swiftdrachen.pupilsdetection.tracking

import by.swiftdrachen.pupilsdetection.utils.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.configs.CascadeClassifierConfig
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class CascadeClassifierDetector(
        private val cascadeClassifier: CascadeClassifier,
        private val config: CascadeClassifierConfig) {

    fun getDetectedRects(sourceMat: Mat): List<Rect> {
        val facesRectMat = MatOfRect()

        val minSize = sourceMat.size()
        minSize.width *= config.minSizeRatio
        minSize.height *= config.minSizeRatio

        val maxSize = sourceMat.size()
        maxSize.width *= config.maxSizeRatio
        maxSize.height *= config.maxSizeRatio

        //TODO: move this out to optimize
        val grayscale = OpenCvUtils.emptyClone(sourceMat)
        Imgproc.cvtColor(sourceMat, grayscale, Imgproc.COLOR_BGRA2GRAY)

        //TODO: move this out to optimize
        Imgproc.equalizeHist(grayscale, grayscale)

        cascadeClassifier.detectMultiScale(
                sourceMat, facesRectMat,
                config.scaleFactor, config.minNeighbours, config.flags,
                minSize, maxSize)

        return facesRectMat.toList()
    }


    fun getFaceMats(sourceMat: Mat): List<Mat> {
        val rects = getDetectedRects(sourceMat)
        val faceRois = mutableListOf<Mat>()

        for (index in rects.indices) {
            val rect = rects[index]
            val roi = sourceMat.submat(rect)
            faceRois.add(roi)
        }

        return faceRois
    }
}
