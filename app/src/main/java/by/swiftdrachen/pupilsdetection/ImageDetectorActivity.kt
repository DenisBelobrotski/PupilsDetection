package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.swiftdrachen.pupilsdetection.tracking.detectors.CascadeClassifierDetector
import by.swiftdrachen.pupilsdetection.tracking.detectors.PupilContourDetector
import by.swiftdrachen.pupilsdetection.tracking.detectors.FacePartDetector
import by.swiftdrachen.pupilsdetection.tracking.configs.EyeCascadeClassifierConfig
import by.swiftdrachen.pupilsdetection.tracking.configs.FaceCascadeClassifierConfig
import by.swiftdrachen.pupilsdetection.tracking.configs.PupilContourDetectorConfig
import by.swiftdrachen.pupilsdetection.utils.FileChooser
import by.swiftdrachen.pupilsdetection.utils.OpenCvUtils
import by.swiftdrachen.pupilsdetection.utils.SessionFileManager

class ImageDetectorActivity : AppCompatActivity() {
    private val imageFileChooser by lazy { FileChooser(this, "image", "*") }
    private val chooseImageButton by lazy { findViewById<Button>(R.id.choose_image_button) }
    private val processImageButton by lazy { findViewById<Button>(R.id.process_image_button) }
    private val sessionFileManager by lazy { SessionFileManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_detector)

        chooseImageButton.setOnClickListener {
            imageFileChooser.choose()
        }

        processImageButton.setOnClickListener {
            processImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageFileChooser.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processImage() {
        val imageUri = imageFileChooser.lastChosenFileUri.value

        if (imageUri == null) {
            Toast.makeText(this, "Bad image uri.", Toast.LENGTH_LONG).show()
            return
        }

        val chosenMat = OpenCvUtils.loadUserMat(this, imageUri)

        if (chosenMat == null) {
            Toast.makeText(this, "Bad loaded mat.", Toast.LENGTH_LONG).show()
            return
        }

        sessionFileManager.saveMat(chosenMat, "source_image")

        //TODO: to gray and histogram equalization
//        val preparedMat = prepareMat(chosenMat)

        val faceCascade = OpenCvUtils.loadCascadeFromAssets(this, FACE_CASCADE_PATH)
        val faceDetectorConfig = FaceCascadeClassifierConfig()

        val eyeCascade = OpenCvUtils.loadCascadeFromAssets(this, EYE_CASCADE_PATH)
        val eyeDetectorConfig = EyeCascadeClassifierConfig()

        val pupilDetectorConfig = PupilContourDetectorConfig()

        if (faceCascade == null || eyeCascade == null) {
            throw Exception("Cascades not loaded.")
        }

        val faceDetector: FacePartDetector = CascadeClassifierDetector(faceCascade, faceDetectorConfig)
        val eyeDetector: FacePartDetector = CascadeClassifierDetector(eyeCascade, eyeDetectorConfig)
        val pupilDetector: FacePartDetector = PupilContourDetector(pupilDetectorConfig)

        faceDetector.targetImage = chosenMat
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
}
