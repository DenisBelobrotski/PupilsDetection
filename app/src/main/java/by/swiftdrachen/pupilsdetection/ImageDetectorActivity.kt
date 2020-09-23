package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.swiftdrachen.pupilsdetection.tracking.CascadeClassifierDetector
import by.swiftdrachen.pupilsdetection.tracking.configs.EyeCascadeClassifierConfig
import by.swiftdrachen.pupilsdetection.tracking.configs.FaceCascadeClassifierConfig
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
        val faceCascadeConfig = FaceCascadeClassifierConfig()

        val eyeCascade = OpenCvUtils.loadCascadeFromAssets(this, EYE_CASCADE_PATH)
        val eyeCascadeConfig = EyeCascadeClassifierConfig()

        if (faceCascade == null || eyeCascade == null) {
            throw Exception("Cascades not loaded.")
        }

        val faceDetector = CascadeClassifierDetector(faceCascade, faceCascadeConfig)
        val eyeDetector = CascadeClassifierDetector(eyeCascade, eyeCascadeConfig)

        val faces = faceDetector.getFaceMats(chosenMat)
        for (faceIndex in faces.indices) {
            val face = faces[faceIndex]
            sessionFileManager.saveMat(face, "detected_face_$faceIndex")

            val eyes = eyeDetector.getFaceMats(face)
            for (eyeIndex in eyes.indices) {
                val eye = eyes[eyeIndex]
                sessionFileManager.saveMat(eye, "detected_eye_${faceIndex}_${eyeIndex}")
            }
        }
    }
}
