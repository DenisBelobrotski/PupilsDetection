package by.swiftdrachen.pupilsdetection

import by.swiftdrachen.pupilsdetection.tracking.detectors.EyePreprocessor
import by.swiftdrachen.pupilsdetection.tracking.detectors.PointDetector
import by.swiftdrachen.pupilsdetection.tracking.detectors.RectDetector
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.utils.SessionFileManager
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class EyeTracker {
    private val processingImage = Mat()
    private val eyePreprocessedImage = Mat()
    private val hsvChannels: MutableList<Mat> = ArrayList(3)

    var sourceImage: Mat? = null
    var faceDetector: RectDetector? = null
    var eyeDetector: RectDetector? = null
    var eyePreprocessor: EyePreprocessor? = null
    var scleraDetector: PointDetector? = null
    var pupilDetector: PointDetector? = null
    var sessionFileManager: SessionFileManager? = null

    private fun saveMat(fileName: String, image: Mat) {
        sessionFileManager!!.saveMat(image, fileName)
    }

    fun detect() {
        val exceptionReason = isDetectionAvailable()
        if (exceptionReason != null) {
            throw EyeTrackerNotPreparedException(exceptionReason)
        }

        val sourceImage = this.sourceImage!!
        val faceDetector = this.faceDetector!!
        val eyeDetector = this.eyeDetector!!
        val eyePreprocessor = this.eyePreprocessor!!
        val scleraDetector = this.scleraDetector!!
        val pupilDetector = this.pupilDetector!!
        val sessionFileManager = this.sessionFileManager!!

        val processingImage = Mat()

        saveMat("rgb", sourceImage)

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_RGB2HSV)

        saveMat("hsv", processingImage)

        val hsvChannels: MutableList<Mat> = ArrayList(3)

        Core.split(processingImage, hsvChannels)

        val hue = hsvChannels[0]
        val saturation = hsvChannels[1]
        val value = hsvChannels[2]

        saveMat("hue", hue)
        saveMat("saturation", saturation)
        saveMat("value", value)

        return

        sessionFileManager.saveMat(sourceImage, "source_image")

        Imgproc.cvtColor(sourceImage, processingImage, Imgproc.COLOR_BGRA2GRAY)
        sessionFileManager.saveMat(processingImage, "gray_image")

        Imgproc.equalizeHist(processingImage, processingImage)
        sessionFileManager.saveMat(processingImage, "hist_equalized_image")

        faceDetector.processingImage = processingImage
        faceDetector.detect()
        val faceRects = faceDetector.detectedRects
        for (faceIndex in faceRects.indices) {
            val faceRect = faceRects[faceIndex]
            val sourceFaceRoi = sourceImage.submat(faceRect)
            val processingFaceRoi = processingImage.submat(faceRect)
            sessionFileManager.saveMat(processingFaceRoi, "detected_face_$faceIndex")

            eyeDetector.processingImage = processingFaceRoi
            eyeDetector.detect()
            val eyeRects = eyeDetector.detectedRects
            for (eyeIndex in eyeRects.indices) {
                val eyeRect = eyeRects[eyeIndex]
                val sourceEyeRoi = sourceFaceRoi.submat(eyeRect)
                val processingEyeRoi = processingFaceRoi.submat(eyeRect)
                sessionFileManager.saveMat(processingEyeRoi, "detected_eye_${faceIndex}_${eyeIndex}")



                eyePreprocessor.prepare(sourceEyeRoi, eyePreprocessedImage, sessionFileManager)

                sessionFileManager.saveMat(eyePreprocessedImage, "preprocessed_eye_${faceIndex}_${eyeIndex}")

                Core.split(eyePreprocessedImage, hsvChannels)

                sessionFileManager.saveMat(hsvChannels[0], "eye_hue_${faceIndex}_${eyeIndex}")
                sessionFileManager.saveMat(hsvChannels[1], "eye_saturation_${faceIndex}_${eyeIndex}")
                sessionFileManager.saveMat(hsvChannels[2], "eye_value_${faceIndex}_${eyeIndex}")

                // TODO: release mats

                hsvChannels.clear()
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

        if (eyePreprocessor == null) {
            return "Eye preprocessor not set"
        }

        if (scleraDetector == null) {
            return "Sclera detector not set"
        }

        if (pupilDetector == null) {
            return "Pupil detector not set"
        }

        if (sessionFileManager == null) {
            return "Session file manager not set"
        }

        return null
    }
}
