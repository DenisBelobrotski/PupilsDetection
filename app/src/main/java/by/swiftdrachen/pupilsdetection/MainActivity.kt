package by.swiftdrachen.pupilsdetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val FACE_IMAGE_PATH = "test_faces/test_face_2.jpg"
const val FACE_CASCADE_PATH = "cascades/haarcascade_frontalface_alt2.xml"
const val EYE_CASCADE_PATH = "cascades/haarcascade_eye_tree_eyeglasses.xml"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val faceImageView = findViewById<ImageView>(R.id.face_image_view)
        val faceImageBitmap = loadBitmap(FACE_IMAGE_PATH)
        faceImageView.setImageBitmap(faceImageBitmap)

        OpenCVLoader.initDebug()

        val faceCascadeClassifier = loadCascadeFromAssets(FACE_CASCADE_PATH)
        val eyeCascadeClassifier = loadCascadeFromAssets(EYE_CASCADE_PATH)

        val processingImage = Mat()
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

            eyeRects.forEach{ eyeRect ->
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
        faceImageView.setImageBitmap(faceImageBitmap)
    }

    private fun loadBitmap(assetPath: String) : Bitmap {
        val assetInputStream = assets.open(assetPath)
        var resultBitmap = BitmapFactory.decodeStream(assetInputStream)
        assetInputStream.close()
        val bitmapConfig = resultBitmap.config

        if (bitmapConfig != Bitmap.Config.ARGB_8888 && bitmapConfig != Bitmap.Config.RGB_565) {
            resultBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, false)
        }

        return resultBitmap
    }

    private fun cacheAssetFile(assetPath: Uri, rewrite: Boolean = false) : File {
        val assetFileName = assetPath.lastPathSegment
        val unpackedFile = File(this.cacheDir, assetFileName)

        if (unpackedFile.exists() && rewrite) {
            unpackedFile.delete()
        }
        unpackedFile.createNewFile()

        assetPath.path?.let {
            val inputStream = assets.open(it)
            val outputStream = FileOutputStream(unpackedFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            inputStream.close()
            outputStream.close()
        }

        return unpackedFile
    }

    private fun loadCascadeFromAssets(assetPath: String) : CascadeClassifier {
        val faceCascadeAssetUri = Uri.parse(assetPath)
        val cachedFaceCascadeFile = cacheAssetFile(faceCascadeAssetUri)

        return loadCascade(cachedFaceCascadeFile)
    }

    private fun loadCascade(cascadeFile: File) :CascadeClassifier {
        val faceCascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
        val isEmpty = faceCascadeClassifier.empty()

        if (isEmpty) {
            throw IOException("Cascade classifier is empty")
        }

        return faceCascadeClassifier
    }
}
