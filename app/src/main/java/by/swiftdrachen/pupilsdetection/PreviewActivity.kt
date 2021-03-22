package by.swiftdrachen.pupilsdetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import by.swiftdrachen.pupilsdetection.tracking.algorithm.EyeTracker
import by.swiftdrachen.pupilsdetection.tracking.config.*
import by.swiftdrachen.pupilsdetection.tracking.cv_util.OpenCvUtils
import by.swiftdrachen.pupilsdetection.tracking.detector.*
import by.swiftdrachen.pupilsdetection.tracking.exception.CascadeClassifierNotLoadedException
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class PreviewActivity : AppCompatActivity() {

    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    var currentImageType = Imgproc.COLOR_RGB2GRAY
    private val textureView by lazy { findViewById<TextureView>(R.id.previewView) }
    private val imageView by lazy { findViewById<ImageView>(R.id.image) }


    //temp
    private var eyeTracker: EyeTracker? = null
    private var faceDetector: FaceCascadeClassifierDetector? = null
    private var eyeDetector: EyeCascadeClassifierDetector? = null
    private var eyePreciser: EyePreciserCenter? = null
    private var pupilDetector: PupilDetector? = null
    private var eyeProcessor: EyeProcessor? = null

    private val tempMat: Mat = Mat()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        if (!checkCameraPermission()) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
        } else {
            startCamera()
        }


        /// temp
        val faceDetectorConfig = FaceCascadeClassifierConfig()
        val faceCascade =
            OpenCvUtils.loadCascadeFromAssets(this, faceDetectorConfig.assetPath)
                ?: throw CascadeClassifierNotLoadedException(faceDetectorConfig.assetPath)
        val faceCascadeClassifierDetector =
            CascadeClassifierDetector(faceCascade, faceDetectorConfig)

        val eyeDetectorConfig = EyeCascadeClassifierConfig()
        val eyeCascade =
            OpenCvUtils.loadCascadeFromAssets(this, eyeDetectorConfig.assetPath)
                ?: throw CascadeClassifierNotLoadedException(eyeDetectorConfig.assetPath)
        val eyeCascadeClassifierDetector = CascadeClassifierDetector(eyeCascade, eyeDetectorConfig)

        val eyeProcessorConfig = EyeProcessorConfig()

        val pupilDetectorConfig = PupilDetectorConfig()

        faceDetector = FaceCascadeClassifierDetector(faceCascadeClassifierDetector)
        eyeDetector = EyeCascadeClassifierDetector(eyeCascadeClassifierDetector)
        eyePreciser = EyePreciserCenter()
        pupilDetector = PupilDetector(pupilDetectorConfig)
        eyeProcessor = EyeProcessor(eyeProcessorConfig, eyePreciser!!, pupilDetector!!)

        val eyeTrackerConfig = EyeTrackerConfig()

        eyeTracker = EyeTracker(eyeTrackerConfig)
        eyeTracker?.faceDetector = faceDetector
        eyeTracker?.eyeDetector = eyeDetector
        eyeTracker?.eyeProcessor = eyeProcessor
    }

    override fun onDestroy() {
        super.onDestroy()

        faceDetector?.clear()
        eyeDetector?.clear()
        eyePreciser?.clear()
        pupilDetector?.clear()
    }

    private fun startCamera() {
        CameraX.unbindAll();
        startPreview()
        startImageAnalysis()

        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    private fun startImageAnalysis(): ImageAnalysis? {
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
                val bitmap = textureView.bitmap ?: return@Analyzer
//                val mat = Mat()
                Log.d("LOOOG", "bit ${bitmap.height}x${bitmap.width}")
                Utils.bitmapToMat(bitmap, tempMat)
                Log.d("LOOOG", "${tempMat.cols()}x${tempMat.rows()}")
//                Imgproc.cvtColor(mat, mat, currentImageType)


                eyeTracker?.sourceImage = tempMat
                eyeTracker?.detect()

                val processedMat = eyeTracker?.sourceImage!!

                Utils.matToBitmap(processedMat, bitmap)
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            }
        return imageAnalysis
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
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (checkCameraPermission()) {
                startPreview()
            } else {
                Toast.makeText(this, "Разрешите пользоваться камерой", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_CAMERA = 1000
    }
}
