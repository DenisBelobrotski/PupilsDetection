package by.swiftdrachen.pupilsdetection.tracking.algorithm

import by.swiftdrachen.pupilsdetection.tracking.configs.EyeTrackerConfig
import by.swiftdrachen.pupilsdetection.tracking.cv_utils.DrawUtils
import by.swiftdrachen.pupilsdetection.tracking.cv_utils.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.detectors.EyeProcessor
import by.swiftdrachen.pupilsdetection.tracking.detectors.RectDetector
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class EyeTracker(private val config: EyeTrackerConfig) {
    private val processingImage = Mat()

    var sourceImage: Mat? = null
    var faceDetector: RectDetector? = null
    var eyeDetector: RectDetector? = null
    var eyeProcessor: EyeProcessor? = null
    var sessionFileManager: SessionFileManager? = null


    fun detect() {
        val exceptionReason = isDetectionAvailable()
        if (exceptionReason != null) {
            throw EyeTrackerNotPreparedException(exceptionReason)
        }

        val sourceImage = this.sourceImage!!
        val faceDetector = this.faceDetector!!
        val eyeDetector = this.eyeDetector!!
        val eyeProcessor = this.eyeProcessor!!

        sessionFileManager?.saveMat(sourceImage, "source_image")

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_RGB2GRAY, 1)
        sessionFileManager?.saveMat(processingImage, "gray_image")

        Imgproc.equalizeHist(processingImage, processingImage)
        sessionFileManager?.saveMat(processingImage, "hist_equalized_image")

        faceDetector.processingImage = processingImage
        faceDetector.detect()
        val faceRects = faceDetector.detectedRects
        for (faceIndex in faceRects.indices) {
            val faceRect = faceRects[faceIndex]
            val sourceFaceRoi = sourceImage.submat(faceRect)
            val processingFaceRoi = processingImage.submat(faceRect)
            sessionFileManager?.saveMat(processingFaceRoi, "detected_face_$faceIndex")

            eyeDetector.processingImage = processingFaceRoi
            eyeDetector.detect()
            val eyeRects = eyeDetector.detectedRects
            for (eyeIndex in eyeRects.indices) {
                val eyeRect = eyeRects[eyeIndex]
                val sourceEyeRoi = sourceFaceRoi.submat(eyeRect)
                val processingEyeRoi = processingFaceRoi.submat(eyeRect)
                sessionFileManager?.saveMat(processingEyeRoi, "detected_eye_${faceIndex}_${eyeIndex}")


                eyeProcessor.sourceImage = sourceEyeRoi
                eyeProcessor.process()


                if (config.drawDebugMarkers) {
                    val roiCenterColor = Scalar(255.0, 255.0, 0.0, 255.0)
                    val eyeCenterColor = Scalar(0.0, 255.0, 0.0, 255.0)
                    val pupilCenterColor = Scalar(255.0, 0.0, 0.0, 255.0)
                    val markerSize = DrawUtils.getMarkerSizeForMat(sourceEyeRoi, 20, 2)
                    val thickness = DrawUtils.getLineThicknessForMat(sourceEyeRoi, 30, 1)
                    val lineType = Imgproc.LINE_8
                    val roiCenter = OpenCvUtils.getMatCenter(sourceEyeRoi)

                    Imgproc.drawMarker(
                            sourceEyeRoi, roiCenter, roiCenterColor,
                            Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
                    Imgproc.drawMarker(
                            sourceEyeRoi, eyeProcessor.detectedEyeCenter, eyeCenterColor,
                            Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
                    Imgproc.drawMarker(
                            sourceEyeRoi, eyeProcessor.detectedPupilCenter, pupilCenterColor,
                            Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
                }


                // TODO: call every few frames (https://github.com/opencv/opencv/issues/4961)
//                System.gc()
            }
        }
    }


    private fun isDetectionAvailable(): String? {
        if (sourceImage == null) {
            return "Target image not set"
        }

        if (faceDetector == null) {
            return "Face detector not set"
        }

        if (eyeDetector == null) {
            return "Eye detector not set"
        }

        if (eyeProcessor == null) {
            return "Eye preprocessor not set"
        }

        return null
    }
}
