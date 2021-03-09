package by.swiftdrachen.pupilsdetection.tracking.detectors

import by.swiftdrachen.pupilsdetection.tracking.configs.EyePreprocessorConfig
import by.swiftdrachen.pupilsdetection.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Range
import org.opencv.imgproc.Imgproc

class EyePreprocessor(private val config: EyePreprocessorConfig) {

    fun prepare(sourceImage: Mat, processedImage: Mat, sessionFileManager: SessionFileManager)
    {
        val rowsCount = sourceImage.rows()
        val colsCount = sourceImage.cols()

        val topOffset = rowsCount * config.topOffsetPercentage / 100
        val bottomOffset = rowsCount * config.bottomOffsetPercentage / 100

        val rowsRange = Range(topOffset, rowsCount - bottomOffset)
        val colsRange = Range(0, colsCount)

        val cutImage = sourceImage.submat(rowsRange, colsRange)

        sessionFileManager.saveMat(cutImage, "cut_eye_bgra")

//        Imgproc.cvtColor(cutImage, processedImage, Imgproc.COLOR_BGRA2BGR)
//        sessionFileManager.saveMat(processedImage, "cut_eye_bgr")

//        Imgproc.cvtColor(processedImage, processedImage, Imgproc.COLOR_BGR2HSV)
//        sessionFileManager.saveMat(processedImage, "cut_eye_hsv")

        // TODO: incorrect conversion
        Imgproc.cvtColor(cutImage, processedImage, Imgproc.COLOR_BGR2HSV)
        sessionFileManager.saveMat(processedImage, "cut_eye_hsv")
    }
}
