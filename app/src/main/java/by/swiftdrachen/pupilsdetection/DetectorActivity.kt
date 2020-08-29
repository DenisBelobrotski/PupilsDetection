package by.swiftdrachen.pupilsdetection

import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

open class DetectorActivity : AppCompatActivity() {
    protected val faceDetector by lazy {
        val faceCascadeClassifier = OpenCvUtils.loadCascadeFromAssets(this, FACE_CASCADE_PATH)
        val eyeCascadeClassifier = OpenCvUtils.loadCascadeFromAssets(this, EYE_CASCADE_PATH)

        if (faceCascadeClassifier == null || eyeCascadeClassifier == null) {
            throw IOException("Bad cascade classifiers")
        }

        FaceDetector(faceCascadeClassifier, eyeCascadeClassifier)
    }
}
