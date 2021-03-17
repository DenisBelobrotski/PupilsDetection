package by.swiftdrachen.pupilsdetection.tracking.algorithm

import by.swiftdrachen.pupilsdetection.tracking.config.EyeTrackerConfig
import by.swiftdrachen.pupilsdetection.tracking.cv_util.DrawUtils
import by.swiftdrachen.pupilsdetection.tracking.cv_util.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.detector.EyeProcessor
import by.swiftdrachen.pupilsdetection.tracking.abstraction.IRectDetector
import by.swiftdrachen.pupilsdetection.tracking.exception.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class EyeTracker(private val config: EyeTrackerConfig) {
    private val processingImage = Mat()

    var sourceImage: Mat? = null
    var faceDetector: IRectDetector? = null
    var eyeDetector: IRectDetector? = null
    var eyeProcessor: EyeProcessor? = null
    var sessionFileManager: SessionFileManager? = null


    fun detect() {
        sessionFileManager?.addLog("EyeTracker - detection started")

        val exceptionReason = isDetectionAvailable()
        if (exceptionReason != null) {
            throw EyeTrackerNotPreparedException(exceptionReason)
        }

        val sourceImage = this.sourceImage!!
        val faceDetector = this.faceDetector!!
        val eyeDetector = this.eyeDetector!!
        val eyeProcessor = this.eyeProcessor!!

        sessionFileManager?.saveMat(sourceImage, "source_image", true)
        sessionFileManager?.addLog("EyeTracker - source image saved", true)

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_RGB2GRAY, 1)
        sessionFileManager?.addLog("EyeTracker - RGB to GRAY done")

        sessionFileManager?.saveMat(processingImage, "gray_image", true)
        sessionFileManager?.addLog("EyeTracker - gray image saved", true)

        Imgproc.equalizeHist(processingImage, processingImage)
        sessionFileManager?.addLog("EyeTracker - image histogram equalized")

        sessionFileManager?.saveMat(processingImage, "hist_equalized_image", true)
        sessionFileManager?.addLog("EyeTracker - histogram equalized image saved", true)

        faceDetector.processingImage = processingImage
        faceDetector.detect()
        sessionFileManager?.addLog("EyeTracker - face detection done")

        val faceRects = faceDetector.detectedRects
        for (faceIndex in faceRects.indices) {
            val faceRect = faceRects[faceIndex]
            val sourceFaceRoi = sourceImage.submat(faceRect)
            val processingFaceRoi = processingImage.submat(faceRect)
            sessionFileManager?.addLog("EyeTracker - face submats taken")

            sessionFileManager?.saveMat(processingFaceRoi, "detected_face_$faceIndex", true)
            sessionFileManager?.addLog("EyeTracker - detected face saved ($faceIndex)", true)

            eyeDetector.processingImage = processingFaceRoi
            eyeDetector.detect()
            sessionFileManager?.addLog("EyeTracker - eye detection done")

            val eyeRects = eyeDetector.detectedRects
            for (eyeIndex in eyeRects.indices) {
                val eyeRect = eyeRects[eyeIndex]
                val sourceEyeRoi = sourceFaceRoi.submat(eyeRect)
                val processingEyeRoi = processingFaceRoi.submat(eyeRect)
                sessionFileManager?.addLog("EyeTracker - eye submats taken")

                sessionFileManager?.saveMat(processingEyeRoi, "detected_eye_${faceIndex}_${eyeIndex}", true)
                sessionFileManager?.addLog("EyeTracker - detected eye saved ($faceIndex) ($eyeIndex)", true)


                eyeProcessor.sourceImage = sourceEyeRoi
                eyeProcessor.process()
                sessionFileManager?.addLog("EyeTracker - eye processed")


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

                    sessionFileManager?.addLog("EyeTracker - debug markers drawn")
                }


                // TODO: call every few frames (https://github.com/opencv/opencv/issues/4961)
//                System.gc()
            }
        }

        sessionFileManager?.addLog("EyeTracker - detection done")
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
