package by.swiftdrachen.pupilsdetection.obsolete

import androidx.appcompat.app.AppCompatActivity
import by.swiftdrachen.pupilsdetection.EYE_CASCADE_PATH
import by.swiftdrachen.pupilsdetection.FACE_CASCADE_PATH
import by.swiftdrachen.pupilsdetection.tracking.cv_util.OpenCvUtils
import java.io.IOException

open class DetectorActivity : AppCompatActivity() {
    protected val faceDetector by lazy {
        val faceCascadeClassifier = OpenCvUtils.loadCascadeFromAssets(this, FACE_CASCADE_PATH)
        val eyeCascadeClassifier = OpenCvUtils.loadCascadeFromAssets(this, EYE_CASCADE_PATH)

        if (faceCascadeClassifier == null || eyeCascadeClassifier == null) {
            throw IOException("Bad cascade classifiers")
        }

        FaceAndEyesDetector(faceCascadeClassifier, eyeCascadeClassifier)
    }
}
