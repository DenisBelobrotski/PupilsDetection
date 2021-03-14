package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.swiftdrachen.pupilsdetection.tracking.algorithm.EyeTracker
import by.swiftdrachen.pupilsdetection.tracking.configs.*
import by.swiftdrachen.pupilsdetection.tracking.cv_utils.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.detectors.*
import by.swiftdrachen.pupilsdetection.tracking.exceptions.CascadeClassifierNotLoadedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import by.swiftdrachen.pupilsdetection.utils.FileChooser

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

        val faceCascade =
            OpenCvUtils.loadCascadeFromAssets(this, FACE_CASCADE_PATH)
                ?: throw CascadeClassifierNotLoadedException(FACE_CASCADE_PATH)
        val faceDetectorConfig = FaceCascadeClassifierConfig()
        val faceCascadeClassifierDetector =
            CascadeClassifierDetector(faceCascade, faceDetectorConfig)

        val eyeCascade =
            OpenCvUtils.loadCascadeFromAssets(this, EYE_CASCADE_PATH)
                ?: throw CascadeClassifierNotLoadedException(EYE_CASCADE_PATH)
        val eyeDetectorConfig = EyeCascadeClassifierConfig()
        val eyeCascadeClassifierDetector = CascadeClassifierDetector(eyeCascade, eyeDetectorConfig)

        val eyeProcessorConfig = EyeProcessorConfig()

        val eyePreciserConfig = EyePreciserHueConfig()
        val pupilDetectorConfig = PupilDetectorConfig()

        val faceDetector = FaceCascadeClassifierDetector(faceCascadeClassifierDetector)
        val eyeDetector = EyeCascadeClassifierDetector(eyeCascadeClassifierDetector)
        val eyePreciser = EyePreciserHue(eyePreciserConfig)
        val pupilDetector = PupilDetector(pupilDetectorConfig)
        val eyeProcessor = EyeProcessor(eyeProcessorConfig, eyePreciser, pupilDetector)

        val eyeTrackerConfig = EyeTrackerConfig()

        val eyeTracker = EyeTracker(eyeTrackerConfig)
        eyeTracker.sourceImage = chosenMat
        eyeTracker.faceDetector = faceDetector
        eyeTracker.eyeDetector = eyeDetector
        eyeTracker.eyeProcessor = eyeProcessor

        eyeTracker.sessionFileManager = sessionFileManager
        eyeProcessor.sessionFileManager = sessionFileManager
        eyePreciser.sessionFileManager = sessionFileManager
        pupilDetector.sessionFileManager = sessionFileManager

        eyeTracker.detect()

        sessionFileManager.saveMat(chosenMat, "result")

        faceDetector.clear()
        eyeDetector.clear()
        eyePreciser.clear()
        pupilDetector.clear()

        Toast.makeText(this, "Detection done", Toast.LENGTH_LONG).show()
    }
}
