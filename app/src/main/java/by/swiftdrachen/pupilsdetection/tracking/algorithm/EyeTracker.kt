package by.swiftdrachen.pupilsdetection.tracking.algorithm

import by.swiftdrachen.pupilsdetection.tracking.abstraction.IEyeTrackerConfig
import by.swiftdrachen.pupilsdetection.tracking.abstraction.IRectDetector
import by.swiftdrachen.pupilsdetection.tracking.cv_util.DrawUtils
import by.swiftdrachen.pupilsdetection.tracking.cv_util.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.detector.EyeProcessor
import by.swiftdrachen.pupilsdetection.tracking.exception.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class EyeTracker(private val config: IEyeTrackerConfig) {
    private val processingImage = Mat()
    private var detectedFaceRect: Rect? = null
    private var detectedLeftEyeRect: Rect? = null
    private var detectedRightEyeRect: Rect? = null

    var sourceImage: Mat? = null
    var faceDetector: IRectDetector? = null
    var eyeDetector: IRectDetector? = null
    var eyeProcessor: EyeProcessor? = null
    var sessionFileManager: SessionFileManager? = null

    val lastDetectedFaceRect: Rect?
        get() = detectedFaceRect
    val lastDetectedLeftEyeRect: Rect?
        get() = detectedLeftEyeRect
    val lastDetectedRightEyeRect: Rect?
        get() = detectedRightEyeRect

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

        detectedFaceRect = getBestFace(faceRects)

        detectedFaceRect?.let { faceRect ->
            val sourceFaceRoi = sourceImage.submat(faceRect)
            val processingFaceRoi = processingImage.submat(faceRect)
            sessionFileManager?.addLog("EyeTracker - face submats taken")

            sessionFileManager?.saveMat(processingFaceRoi, "detected_face", true)
            sessionFileManager?.addLog("EyeTracker - detected face saved", true)

            eyeDetector.processingImage = processingFaceRoi
            eyeDetector.detect()
            sessionFileManager?.addLog("EyeTracker - eye detection done")

            tryDrawFaceRect(sourceImage, faceRect)


            val eyeRects = eyeDetector.detectedRects
            val eyes = getBestEyes(eyeRects, sourceFaceRoi)

            detectedLeftEyeRect = eyes.first
            detectedRightEyeRect = eyes.second

            detectedLeftEyeRect?.let { leftEyeRect ->
                processEye(leftEyeRect, eyeProcessor, sourceFaceRoi, processingFaceRoi, true)
            }
            detectedRightEyeRect?.let { rightEyeRect ->
                processEye(rightEyeRect, eyeProcessor, sourceFaceRoi, processingFaceRoi, false)
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


    private fun processEye(
            eyeRect: Rect, eyeProcessor: EyeProcessor,
            sourceFaceRoi: Mat, processingFaceRoi: Mat,
            left: Boolean) {
        val sourceEyeRoi = sourceFaceRoi.submat(eyeRect)
        val processingEyeRoi = processingFaceRoi.submat(eyeRect)
        sessionFileManager?.addLog("EyeTracker - eye submats taken")

        val eyeName = if (left) "left" else "right"
        sessionFileManager?.saveMat(processingEyeRoi, "detected_eye_$eyeName", true)
        sessionFileManager?.addLog("EyeTracker - detected eye saved ($eyeName)", true)


        eyeProcessor.sourceImage = sourceEyeRoi
        eyeProcessor.process()
        sessionFileManager?.addLog("EyeTracker - eye processed")


        tryDrawEyeRect(sourceFaceRoi, eyeRect, left)
        tryDrawEyeMarkers(sourceEyeRoi, eyeProcessor)
    }


    private fun tryDrawFaceRect(sourceImage: Mat, faceRect: Rect) {
        if (config.drawDebugFaceRects) {
            val thickness = DrawUtils.getLineThicknessForMat(sourceImage, 100, 1)
            val lineType = Imgproc.LINE_8
            Imgproc.rectangle(sourceImage, faceRect, faceRectColor, thickness, lineType)
        }
    }


    private fun tryDrawEyeRect(sourceFaceRoi: Mat, eyeRect: Rect, left: Boolean) {
        if (config.drawDebugEyeRects) {
            val color = if (left) leftEyeRectColor else rightEyeRectColor
            val thickness = DrawUtils.getLineThicknessForMat(sourceFaceRoi, 100, 1)
            val lineType = Imgproc.LINE_8
            Imgproc.rectangle(sourceFaceRoi, eyeRect, color, thickness, lineType)
        }
    }


    private fun tryDrawEyeMarkers(sourceEyeRoi: Mat, eyeProcessor: EyeProcessor) {
        if (config.drawDebugEyeMarkers) {
            val markerSize = DrawUtils.getMarkerSizeForMat(sourceEyeRoi, 20, 2)
            val thickness = DrawUtils.getLineThicknessForMat(sourceEyeRoi, 30, 1)
            val lineType = Imgproc.LINE_8
            val roiCenter = OpenCvUtils.getMatCenter(sourceEyeRoi)

            Imgproc.drawMarker(
                    sourceEyeRoi, roiCenter, eyeRoiCenterColor,
                    Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
            Imgproc.drawMarker(
                    sourceEyeRoi, eyeProcessor.detectedEyeCenter, eyeCenterColor,
                    Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)
            Imgproc.drawMarker(
                    sourceEyeRoi, eyeProcessor.detectedPupilCenter, pupilCenterColor,
                    Imgproc.MARKER_DIAMOND, markerSize, thickness, lineType)

            sessionFileManager?.addLog("EyeTracker - debug markers drawn")
        }
    }


    private fun getBestFace(faceRects: List<Rect>): Rect? {
        val rectsCount = faceRects.count()

        if (rectsCount == 0) {
            return null
        }

        var relevantFaceRect = faceRects[0]
        var maxArea = relevantFaceRect.area()

        for (index in 1 until rectsCount) {
            val currentRect = faceRects[index]
            val currentArea = currentRect.area()

            if (currentArea > maxArea) {
                maxArea = currentArea
                relevantFaceRect = currentRect
            }
        }

        return relevantFaceRect
    }


    private fun getBestEyes(eyeRects: List<Rect>, faceMat: Mat): Pair<Rect?, Rect?> {
        val rectsCount = eyeRects.count()

        if (rectsCount == 0) {
            return Pair(null, null)
        }

        var leftEye: Rect? = null
        var rightEye: Rect? = null

        val faceCenter = OpenCvUtils.getMatCenter(faceMat)

        for (index in 0 until rectsCount) {
            val eyeRect = eyeRects[index]
            val eyeCenter = OpenCvUtils.getRectCenter(eyeRect)

            if (eyeCenter.y > faceCenter.y) {
                continue
            }

            if (leftEye == null && eyeCenter.x < faceCenter.x) {
                leftEye = eyeRect
                continue
            }

            if (rightEye == null && eyeCenter.x > faceCenter.x) {
                rightEye = eyeRect
                continue
            }
        }

        return if (!config.mirrorEyes) {
            Pair(leftEye, rightEye)
        } else {
            Pair(rightEye, leftEye)
        }
    }


    companion object {
        val faceRectColor = Scalar(0.0, 255.0, 0.0, 255.0)

        val leftEyeRectColor = Scalar(255.0, 0.0, 255.0, 255.0)
        val rightEyeRectColor = Scalar(0.0, 255.0, 255.0, 255.0)

        val eyeRoiCenterColor = Scalar(255.0, 255.0, 0.0, 255.0)
        val eyeCenterColor = Scalar(0.0, 255.0, 0.0, 255.0)
        val pupilCenterColor = Scalar(255.0, 0.0, 0.0, 255.0)
    }
}
