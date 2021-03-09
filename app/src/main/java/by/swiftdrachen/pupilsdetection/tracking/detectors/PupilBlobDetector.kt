package by.swiftdrachen.pupilsdetection.tracking.detectors

import android.util.Log
import by.swiftdrachen.pupilsdetection.tracking.configs.PupilBlobDetectorConfig
import by.swiftdrachen.pupilsdetection.tracking.exceptions.DetectorNotPreparedException
import by.swiftdrachen.pupilsdetection.utils.OpenCvUtils
import by.swiftdrachen.pupilsdetection.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.features2d.Feature2D
import org.opencv.features2d.Features2d
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.imgproc.Imgproc

class PupilBlobDetector(
        private val config: PupilBlobDetectorConfig,
        private val sessionFileManager: SessionFileManager) : FacePartDetector {
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

//        JavaCV
//        val toJavaCvMat = OpenCVFrameConverter.ToMat()
//        val toOpenCvMat = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
//        val sourceImage = toJavaCvMat.convert(toOpenCvMat.convert(targetImage as Mat))
//        val params = SimpleBlobDetector.Params()

        val threshold = 85.0

        val sourceImage = targetImage as Mat
        var processingImage = OpenCvUtils.emptyClone(sourceImage)

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_BGRA2GRAY)
//        Imgproc.equalizeHist(processingImage, processingImage)
        Imgproc.threshold(processingImage, processingImage, threshold, 255.0, Imgproc.THRESH_BINARY)

        processingImage = cutBrow(processingImage)

        val detector = SimpleBlobDetector.create()
        val keyPointsMat = MatOfKeyPoint()
        detector.detect(processingImage, keyPointsMat)
        val keyPoints = keyPointsMat.toList()

        Log.d("tvar", "key points: ${keyPoints.count()}")

        val color = Scalar(0.0, 0.0, 255.0)

        Features2d.drawKeypoints(processingImage, keyPointsMat, processingImage, color, Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS)


        mutableDetectedImages.add(processingImage)
    }


    override fun clear() {
        mutableDetectedRects.clear()
        mutableDetectedImages.clear()
    }


    private fun cutBrow(sourceImage: Mat): Mat {
        val imageHeight = sourceImage.height()
        val imageWidth = sourceImage.width()
        val browOffset = (imageHeight.toDouble() * 0.2).toInt()

        return sourceImage.submat(browOffset, imageHeight, 0, imageWidth)
    }
}
