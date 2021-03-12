package by.swiftdrachen.pupilsdetection.tracking.cv_utils

import org.bytedeco.javacv.OpenCVFrameConverter

class MatConverter {
    private val frameToJavaCvMat = OpenCVFrameConverter.ToMat()
    private val frameToOpenCvMat = OpenCVFrameConverter.ToOrgOpenCvCoreMat()

    fun convert(javaCvMat: org.bytedeco.opencv.opencv_core.Mat): org.opencv.core.Mat {
        return frameToOpenCvMat.convert(frameToJavaCvMat.convert(javaCvMat))
    }

    fun convert(openCvMat: org.opencv.core.Mat): org.bytedeco.opencv.opencv_core.Mat {
        return frameToJavaCvMat.convert(frameToOpenCvMat.convert(openCvMat))
    }
}
