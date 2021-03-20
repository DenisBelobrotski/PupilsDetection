package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.swiftdrachen.pupilsdetection.tracking.algorithm.EyeTracker
import by.swiftdrachen.pupilsdetection.tracking.config.*
import by.swiftdrachen.pupilsdetection.tracking.cv_util.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.detector.*
import by.swiftdrachen.pupilsdetection.tracking.exception.CascadeClassifierNotLoadedException
import by.swiftdrachen.pupilsdetection.tracking.utils.SessionFileManager
import by.swiftdrachen.pupilsdetection.utils.FileChooser

class ImageDetectorActivity : AppCompatActivity() {
    private val imageFileChooser by lazy { FileChooser(this, "image", "*") }
    private val resultImageView by lazy { findViewById<ImageView>(R.id.result_image_view) }
    private val newSessionButton by lazy { findViewById<Button>(R.id.new_session_button) }
    private val chooseImageButton by lazy { findViewById<Button>(R.id.choose_image_button) }
    private val processImageButton by lazy { findViewById<Button>(R.id.process_image_button) }

    private var sessionFileManager: SessionFileManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_detector)

        newSessionButton.setOnClickListener {
            sessionFileManager = SessionFileManager(this)
        }

        chooseImageButton.setOnClickListener {
            imageFileChooser.choose()
        }

        processImageButton.setOnClickListener {
            processImage()
        }

        sessionFileManager = SessionFileManager(this)
//        sessionFileManager?.isDebugImageSavingEnabled = true
//        sessionFileManager?.isDebugLogSavingEnabled = true
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

        val pupilDetectorConfig = PupilDetectorConfig()

        val faceDetector = FaceCascadeClassifierDetector(faceCascadeClassifierDetector)
        val eyeDetector = EyeCascadeClassifierDetector(eyeCascadeClassifierDetector)
        val eyePreciser = EyePreciserCenter()
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


        val imageWidth = chosenMat.cols()
        val imageHeight = chosenMat.rows()

        sessionFileManager?.addLog("ImageDetectorActivity - detection started (${imageWidth}x${imageHeight})")
        eyeTracker.detect()
        sessionFileManager?.addLog("ImageDetectorActivity - detection done")
        sessionFileManager?.saveLogFile("tracking_time")

        sessionFileManager?.addLog("ImageDetectorActivity - result saving started")
        sessionFileManager?.saveMat(chosenMat, "result")
        sessionFileManager?.addLog("ImageDetectorActivity - result saving done")
        sessionFileManager?.saveLogFile("tracking_output_time")

        eyeTracker.sourceImage?.let {
            val resultBitmap = OpenCvUtils.getBitmapFromMat(it)
            resultImageView.setImageBitmap(resultBitmap)
        }

        faceDetector.clear()
        eyeDetector.clear()
        eyePreciser.clear()
        pupilDetector.clear()

        Toast.makeText(this, "Detection done", Toast.LENGTH_LONG).show()
    }
}
