package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.osgi.OpenCVNativeLoader
import java.io.File
import java.io.IOException

private const val FACE_IMAGE_PATH = "test_faces/test_face_2.jpg"
private const val FACE_CASCADE_PATH = "cascades/haarcascade_frontalface_alt2.xml"
private const val EYE_CASCADE_PATH = "cascades/haarcascade_eye_tree_eyeglasses.xml"

class MainActivity : AppCompatActivity() {

    private val videoFileChooser by lazy { VideoFileChooser(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        loadOpenCV()

        val faceCascadeClassifier = loadCascadeFromAssets(FACE_CASCADE_PATH)
        val eyeCascadeClassifier = loadCascadeFromAssets(EYE_CASCADE_PATH)

        if (faceCascadeClassifier == null || eyeCascadeClassifier == null) {
            throw IOException("Bad cascade classifiers")
        }

        val processingImage = Mat()
        val faceImageBitmap = FileSystemUtils.loadBitmapResource(this, FACE_IMAGE_PATH)
        Utils.bitmapToMat(faceImageBitmap, processingImage)

        val facesRectMat = MatOfRect()
        faceCascadeClassifier.detectMultiScale(processingImage, facesRectMat)
        val faceRects = facesRectMat.toList()

        faceRects.forEach { faceRect ->
            val faceHalfSize = Size(
                faceRect.width.toDouble() * 0.5,
                faceRect.height.toDouble() * 0.5)
            val faceCenter = Point(
                faceRect.x.toDouble() + faceHalfSize.width,
                faceRect.y.toDouble() + faceHalfSize.height)
            val faceColor = Scalar(1.0, 1.0, 1.0, 1.0)

            Imgproc.circle(processingImage, faceCenter, faceRect.width / 2, faceColor, 10, 8, 0);

            val faceROI = processingImage.submat(faceRect)
            val eyesRectMat = MatOfRect()
            eyeCascadeClassifier.detectMultiScale(faceROI, eyesRectMat)
            val eyeRects = eyesRectMat.toList()

            eyeRects.forEach { eyeRect ->
                val eyeHalfSize = Size(
                    eyeRect.width.toDouble() * 0.5,
                    eyeRect.height.toDouble() * 0.5)
                val eyeCenter = Point(
                    faceRect.x.toDouble() + eyeRect.x.toDouble() + eyeHalfSize.width,
                    faceRect.y.toDouble() + eyeRect.y.toDouble() + eyeHalfSize.height)
                val eyeColor = Scalar(1.0, 1.0, 1.0, 1.0)

                Imgproc.circle(processingImage, eyeCenter, eyeRect.width / 2, eyeColor, 5, 8, 0);
            }
        }

        Utils.matToBitmap(processingImage, faceImageBitmap)

        val faceImageView = findViewById<ImageView>(R.id.face_image_view)
        faceImageView.setImageBitmap(faceImageBitmap)

        val chooseVideoButton = findViewById<Button>(R.id.choose_video_button)
        chooseVideoButton.setOnClickListener {
            videoFileChooser.choose()
        }

        val processVideoButton = findViewById<Button>(R.id.process_video_button)
        processVideoButton.setOnClickListener {
            processVideo()
        }
    }

    private fun loadOpenCV() {
        val loader = OpenCVNativeLoader()
        loader.init()
    }

    private fun processVideo() {
        val grabber = FFmpegFrameGrabber(videoFileChooser.lastChosenFile.value)

        try {
            grabber.start()
        } catch (exception: FrameGrabber.Exception) {
            Toast.makeText(this, "Failed to start grabber.", Toast.LENGTH_LONG).show();
        }

        var frame: Frame? = null
        var framesCount = 0

        do {
            try {
                frame = grabber.grabFrame()
                if (frame != null) {
                    framesCount += 1
                }
            } catch (exception: FrameGrabber.Exception) {
                Toast.makeText(this, "Failed to grab frame.", Toast.LENGTH_LONG).show();
            }
        } while (frame != null)

        Toast.makeText(this,
            "video format: ${grabber.format}\n" +
                "pixel format: ${grabber.pixelFormat}\n" +
                "sample format: ${grabber.sampleFormat}",
            Toast.LENGTH_LONG).show();

        try {
            grabber.stop()
        } catch (exception: FrameGrabber.Exception) {
            Toast.makeText(this, "Failed to stop grabber.", Toast.LENGTH_LONG).show();
        }

        videoFileChooser.lastChosenFile.value?.let {
            val fileSizeMb: Double = it.length().toDouble() / (1024.0 * 1024.0)
            Toast.makeText(this, "file size: %.2f".format(fileSizeMb), Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this, "frames count: $framesCount", Toast.LENGTH_LONG).show();
    }

    private fun loadCascadeFromAssets(assetPath: String): CascadeClassifier? {
        val faceCascadeAssetUri = Uri.parse(assetPath)
        val cachedFaceCascadeFile = FileSystemUtils.cacheAssetFile(this, faceCascadeAssetUri)
        var loadedCascade: CascadeClassifier? = null
        cachedFaceCascadeFile?.let {
            loadedCascade = loadCascade(cachedFaceCascadeFile)
        }

        return loadedCascade
    }

    private fun loadCascade(cascadeFile: File): CascadeClassifier {
        val faceCascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
        val isEmpty = faceCascadeClassifier.empty()

        if (isEmpty) {
            throw IOException("Cascade classifier is empty")
        }

        return faceCascadeClassifier
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        videoFileChooser.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }
}
