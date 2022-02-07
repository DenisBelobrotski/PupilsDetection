package com.denisbelobrotski.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import com.denisbelobrotski.eye_tracking_library.algorithm.EyeTracker
import com.denisbelobrotski.eye_tracking_library.config.*
import com.denisbelobrotski.eye_tracking_library.cv_util.OpenCvUtils
import com.denisbelobrotski.eye_tracking_library.detector.CascadeClassifierDetector
import com.denisbelobrotski.eye_tracking_library.detector.EyePreciserCenter
import com.denisbelobrotski.eye_tracking_library.detector.EyeProcessor
import com.denisbelobrotski.eye_tracking_library.detector.PupilDetector
import com.denisbelobrotski.eye_tracking_library.exception.CascadeClassifierNotLoadedException
import com.denisbelobrotski.eye_tracking_library.util.SessionFileManager
import com.denisbelobrotski.pupilsdetection.utils.FileChooser
import com.denisbelobrotski.pupilsdetection.utils.ResultUtils

class ImageDetectorActivity : AppCompatActivity() {
    private val imageFileChooser by lazy { FileChooser(this, "image", "*") }

    private val resultImageView by lazy { findViewById<ImageView>(R.id.result_image_view) }

    private val leftEyeStatusView by lazy { findViewById<TextView>(R.id.left_eye_direction_status) }
    private val rightEyeStatusView by lazy { findViewById<TextView>(R.id.right_eye_direction_status) }

    private val gazeCenterValueView by lazy { findViewById<TextView>(R.id.gaze_center_value) }
    private val gazeCenterSliderView by lazy { findViewById<AppCompatSeekBar>(R.id.gaze_center_slider) }

    private val newSessionButton by lazy { findViewById<Button>(R.id.new_session_button) }
    private val clearSessionButton by lazy { findViewById<Button>(R.id.clear_session_button) }
    private val chooseImageButton by lazy { findViewById<Button>(R.id.choose_image_button) }
    private val processImageButton by lazy { findViewById<Button>(R.id.process_image_button) }

    private var sessionFileManager: SessionFileManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_detector)

        newSessionButton.setOnClickListener {
            sessionFileManager = SessionFileManager(this)
        }

        clearSessionButton.setOnClickListener {
            sessionFileManager = null
        }

        chooseImageButton.setOnClickListener {
            imageFileChooser.choose()
        }

        processImageButton.setOnClickListener {
            processImage()
        }

//        sessionFileManager = SessionFileManager(this)
//        sessionFileManager?.isDebugImageSavingEnabled = true
//        sessionFileManager?.isDebugLogSavingEnabled = true

        initGazeCenterViews(gazeCenterValueView, gazeCenterSliderView)
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

        val faceDetectorConfig = FaceCascadeClassifierConfig()
//        faceDetectorConfig.minSizeRatio = 0.0
//        faceDetectorConfig.maxSizeRatio = 1.0
        val faceCascade =
            OpenCvUtils.loadCascadeFromAssets(this, faceDetectorConfig.assetPath)
                ?: throw CascadeClassifierNotLoadedException(faceDetectorConfig.assetPath)
        val faceDetector =
            CascadeClassifierDetector(faceCascade, faceDetectorConfig)

        val eyeDetectorConfig = EyeCascadeClassifierConfig()
//        eyeDetectorConfig.minSizeRatio = 0.0
//        eyeDetectorConfig.maxSizeRatio = 1.0
        val eyeCascade =
            OpenCvUtils.loadCascadeFromAssets(this, eyeDetectorConfig.assetPath)
                ?: throw CascadeClassifierNotLoadedException(eyeDetectorConfig.assetPath)
        val eyeDetector = CascadeClassifierDetector(eyeCascade, eyeDetectorConfig)

        val eyeProcessorConfig = EyeProcessorConfig()

        val pupilDetectorConfig = PupilDetectorConfig()

        val eyePreciser = EyePreciserCenter()
        eyePreciser.sessionFileManager = sessionFileManager

        val pupilDetector = PupilDetector(pupilDetectorConfig)
        pupilDetector.sessionFileManager = sessionFileManager

        val eyeProcessor = EyeProcessor(eyeProcessorConfig, eyePreciser, pupilDetector)
        eyeProcessor.sessionFileManager = sessionFileManager

        val eyeTrackerConfig = EyeTrackerConfig(faceDetector, eyeDetector, eyeProcessor)
        eyeTrackerConfig.sessionFileManager = sessionFileManager
        eyeTrackerConfig.gazeCenterDirectionOffset = gazeCenterSliderView.progress

        val eyeTracker = EyeTracker(eyeTrackerConfig)
        eyeTracker.sourceImage = chosenMat


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


        ResultUtils.updateEyeStatus(
                leftEyeStatusView, eyeTracker.leftEyeBestGazeDirectionIndex, eyeTrackerConfig)
        ResultUtils.updateEyeStatus(
                rightEyeStatusView, eyeTracker.rightEyeBestGazeDirectionIndex, eyeTrackerConfig)


        faceDetector.clear()
        eyeDetector.clear()
        eyePreciser.clear()
        pupilDetector.clear()

        Toast.makeText(this, "Detection done", Toast.LENGTH_LONG).show()
    }


    private fun initGazeCenterViews(valueView: TextView, sliderView: SeekBar) {
        valueView.text = "${sliderView.progress}%"

        sliderView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueView.text = "${progress}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
