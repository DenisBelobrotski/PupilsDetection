package com.denisbelobrotski.pupilsdetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denisbelobrotski.eye_tracking_library.algorithm.EyeTracker
import com.denisbelobrotski.eye_tracking_library.config.*
import com.denisbelobrotski.eye_tracking_library.cv_util.OpenCvUtils
import com.denisbelobrotski.eye_tracking_library.detector.*
import com.denisbelobrotski.eye_tracking_library.exception.CascadeClassifierNotLoadedException
import com.denisbelobrotski.pupilsdetection.utils.ResultUtils
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.time.Duration
import java.time.LocalDateTime

class PreviewActivity : AppCompatActivity() {

    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val textureView by lazy { findViewById<TextureView>(R.id.previewView) }
    private val imageView by lazy { findViewById<ImageView>(R.id.image) }

    private val leftEyeStatusView by lazy { findViewById<TextView>(R.id.left_eye_direction_status) }
    private val rightEyeStatusView by lazy { findViewById<TextView>(R.id.right_eye_direction_status) }

    private val gazeCenterValueView by lazy { findViewById<TextView>(R.id.gaze_center_value) }
    private val gazeCenterSliderView by lazy { findViewById<AppCompatSeekBar>(R.id.gaze_center_slider) }

    private val fpsMeterView by lazy { findViewById<TextView>(R.id.fps_meter) }


    private var eyeTrackerConfig: EyeTrackerConfig? = null
    private var eyeTracker: EyeTracker? = null
    private var faceDetector: CascadeClassifierDetector? = null
    private var eyeDetector: CascadeClassifierDetector? = null
    private var eyePreciser: EyePreciserCenter? = null
//    private var eyePreciser: EyePreciserSaturation? = null
    private var pupilDetector: PupilDetector? = null
    private var eyeProcessor: EyeProcessor? = null

    private val tempMat: Mat = Mat()

    private var tempDate: LocalDateTime? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        if (!checkCameraPermission()) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
        } else {
            startCamera()
        }


        initEyeTracker()
        initGazeCenterViews(gazeCenterValueView, gazeCenterSliderView, eyeTrackerConfig!!)
    }


    override fun onDestroy() {
        super.onDestroy()

        faceDetector?.clear()
        eyeDetector?.clear()
        eyePreciser?.clear()
        pupilDetector?.clear()
    }


    private fun startCamera() {
        CameraX.unbindAll()
        startPreview()
        startImageAnalysis()

        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }


    private fun initEyeTracker() {
        val faceDetectorConfig = FaceCascadeClassifierConfig()
        val faceCascade =
            OpenCvUtils.loadCascadeFromAssets(this, faceDetectorConfig.assetPath)
                ?: throw CascadeClassifierNotLoadedException(faceDetectorConfig.assetPath)
        faceDetector = CascadeClassifierDetector(faceCascade, faceDetectorConfig)

        val eyeDetectorConfig = EyeCascadeClassifierConfig()
        val eyeCascade =
            OpenCvUtils.loadCascadeFromAssets(this, eyeDetectorConfig.assetPath)
                ?: throw CascadeClassifierNotLoadedException(eyeDetectorConfig.assetPath)
        eyeDetector = CascadeClassifierDetector(eyeCascade, eyeDetectorConfig)

        val eyeProcessorConfig = EyeProcessorConfig()
        val pupilDetectorConfig = PupilDetectorConfig()
//        val eyePreciserConfig = EyePreciserSaturationConfig()

        eyePreciser = EyePreciserCenter()
//        eyePreciser = EyePreciserSaturation(eyePreciserConfig)
        pupilDetector = PupilDetector(pupilDetectorConfig)
        eyeProcessor = EyeProcessor(eyeProcessorConfig, eyePreciser!!, pupilDetector!!)

        eyeTrackerConfig = EyeTrackerConfig(faceDetector!!, eyeDetector!!, eyeProcessor!!)

        eyeTracker = EyeTracker(eyeTrackerConfig!!)
    }


    private fun initGazeCenterViews(
            valueView: TextView, sliderView: SeekBar, eyeTrackerConfig: EyeTrackerConfig) {
        sliderView.progress = eyeTrackerConfig.gazeCenterDirectionOffset

        valueView.text = "${sliderView.progress}%"

        sliderView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueView.text = "${progress}%"
                eyeTrackerConfig.gazeCenterDirectionOffset = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private fun startImageAnalysis(): ImageAnalysis {
        val analyzerThread = HandlerThread("Analysis")
        analyzerThread.start()

        val imageAnalysisConfig =
            ImageAnalysisConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setTargetResolution(Size(textureView.width, textureView.height))
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(Handler(analyzerThread.looper))
                .setImageQueueDepth(1)
                .build()

        imageAnalysis = ImageAnalysis(imageAnalysisConfig)
        imageAnalysis.analyzer =
            ImageAnalysis.Analyzer { _, _ ->
                checkFps()

                val bitmap = textureView.bitmap ?: return@Analyzer

                Utils.bitmapToMat(bitmap, tempMat)

                eyeTracker?.sourceImage = tempMat
                eyeTracker?.detect()

                val processedMat = eyeTracker?.sourceImage!!

                Utils.matToBitmap(processedMat, bitmap)
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)

                    if (eyeTracker != null && eyeTrackerConfig != null) {
                        ResultUtils.updateEyeStatus(leftEyeStatusView,
                                eyeTracker!!.leftEyeBestGazeDirectionIndex, eyeTrackerConfig!!)
                        ResultUtils.updateEyeStatus(rightEyeStatusView,
                                eyeTracker!!.rightEyeBestGazeDirectionIndex, eyeTrackerConfig!!)
                    }
                }
            }

        return imageAnalysis
    }


    private fun checkFps() {
        val currentDate = LocalDateTime.now()
        tempDate?.let {
            val delta = Duration.between(it, currentDate)
            val frameRate = delta.toMillis()
            val fps = (1.0 / (frameRate * 0.001)).toInt()

            runOnUiThread {
                fpsMeterView.text = "$fps fps"
            }
        }
        tempDate = currentDate
    }


    private fun startPreview() {
        val aspectRatio = Rational(textureView.width, textureView.height)
        val screen = Size(textureView.width, textureView.height)

        val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetResolution(screen)
            .setLensFacing(CameraX.LensFacing.FRONT)
            .build()

        preview = Preview(previewConfig)

        preview.onPreviewOutputUpdateListener = Preview.OnPreviewOutputUpdateListener { output ->
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)
            textureView.setSurfaceTexture(output.surfaceTexture)
            updateTransform()
        }
    }


    private fun updateTransform() {
        val mx = Matrix()
        val w = textureView.measuredWidth.toFloat()
        val h = textureView.measuredHeight.toFloat()
        val cX = w / 2f
        val cY = h / 2f
        val rotationDgr: Int
        val rotation = textureView.rotation.toInt()
        rotationDgr = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        mx.postRotate(rotationDgr.toFloat(), cX, cY)
        textureView.setTransform(mx)
    }


    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (checkCameraPermission()) {
                startPreview()
            } else {
                Toast.makeText(this, "No camera access", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }


    companion object {
        private const val REQUEST_CODE_CAMERA = 1000
    }
}
