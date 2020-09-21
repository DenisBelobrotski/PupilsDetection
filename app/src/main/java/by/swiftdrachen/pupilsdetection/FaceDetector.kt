package by.swiftdrachen.pupilsdetection

import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class FaceDetector(private val cascadeClassifier: CascadeClassifier) {
    companion object {
        private const val ScaleFactor = 1.1
        private const val MinNeighbours = 4
        private const val Flags = 0
        private const val MinSizeRatio = 0.2
        private const val MaxSizeRatio = 0.5
    }


    fun getFaceRects(sourceMat: Mat): List<Rect> {
        val facesRectMat = MatOfRect()

        val minSize = sourceMat.size()
        minSize.width *= MinSizeRatio
        minSize.height *= MinSizeRatio

        val maxSize = sourceMat.size()
        maxSize.width *= MaxSizeRatio
        maxSize.height *= MaxSizeRatio

        //TODO: move this out to optimize
        val grayscale = OpenCvUtils.emptyClone(sourceMat)
        Imgproc.cvtColor(sourceMat, grayscale, Imgproc.COLOR_BGRA2GRAY)

        //TODO: move this out to optimize
        Imgproc.equalizeHist(grayscale, grayscale)

        cascadeClassifier.detectMultiScale(
                sourceMat, facesRectMat, ScaleFactor, MinNeighbours, Flags, minSize, maxSize)

        return facesRectMat.toList()
    }


    fun getFaceMats(sourceMat: Mat): List<Mat> {
        val rects = getFaceRects(sourceMat)
        val faceRois = mutableListOf<Mat>()

        for (index in rects.indices) {
            val rect = rects[index]
            val roi = sourceMat.submat(rect)
            faceRois.add(roi)
        }

        return faceRois
    }
}
