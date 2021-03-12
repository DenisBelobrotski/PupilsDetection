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

        Imgproc.cvtColor(cutImage, processedImage, Imgproc.COLOR_RGB2HSV)
    }
}
