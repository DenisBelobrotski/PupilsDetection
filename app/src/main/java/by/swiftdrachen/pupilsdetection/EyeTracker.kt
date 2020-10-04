package by.swiftdrachen.pupilsdetection

import by.swiftdrachen.pupilsdetection.tracking.detectors.FacePartDetector
import by.swiftdrachen.pupilsdetection.tracking.exceptions.EyeTrackerNotPreparedException
import by.swiftdrachen.pupilsdetection.utils.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Rect

class EyeTracker {
    var targetImage: Mat? = null
    var faceDetector: FacePartDetector? = null
    var eyeDetector: FacePartDetector? = null
    var pupilDetector: FacePartDetector? = null
    var sessionFileManager: SessionFileManager? = null

    private var mutableLeftPupilRect: Rect? = null
    private var mutableRightPupilRect: Rect? = null

    val leftPupilRect: Rect?
        get() = mutableLeftPupilRect

    val rightPupilRect: Rect?
        get() = mutableRightPupilRect


    fun detect() {
        val exceptionReason = isDetectionAvailable()
        if (exceptionReason != null) {
            throw EyeTrackerNotPreparedException(exceptionReason)
        }

        val faceDetector = this.faceDetector!!
        val eyeDetector = this.eyeDetector!!
        val pupilDetector = this.pupilDetector!!
        val sessionFileManager = this.sessionFileManager!!

        faceDetector.targetImage = targetImage
        faceDetector.detect()
        val faces = faceDetector.detectedImages
        for (faceIndex in faces.indices) {
            val face = faces[faceIndex]
            sessionFileManager.saveMat(face, "detected_face_$faceIndex")

            eyeDetector.targetImage = face
            eyeDetector.detect()
            val eyes = eyeDetector.detectedImages
            for (eyeIndex in eyes.indices) {
                val eye = eyes[eyeIndex]
                sessionFileManager.saveMat(eye, "detected_eye_${faceIndex}_${eyeIndex}")

                pupilDetector.targetImage = eye
                pupilDetector.detect()
                val pupils = pupilDetector.detectedImages
                for (pupilIndex in pupils.indices) {
                    val pupil = pupils[pupilIndex]
                    sessionFileManager.saveMat(pupil, "detected_pupil_${faceIndex}_${eyeIndex}_${pupilIndex}")
                }
            }
        }
    }


    private fun isDetectionAvailable(): String? {
        if (targetImage == null) {
            return "Target image not set"
        }

        if (faceDetector == null) {
            return "Face detector not set"
        }

        if (eyeDetector == null) {
            return "Eye detector not set"
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
