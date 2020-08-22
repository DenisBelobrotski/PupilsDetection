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

const val FACE_IMAGE_PATH = "test_faces/test_face_3.jpg"
const val FACE_CASCADE_PATH = "cascades/haarcascade_frontalface_alt2.xml"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val faceImageView = findViewById<ImageView>(R.id.face_image_view)
        val faceImageBitmap = loadBitmap(FACE_IMAGE_PATH)
        faceImageView.setImageBitmap(faceImageBitmap)

        OpenCVLoader.initDebug()

        val faceCascadeAssetUri = Uri.parse(FACE_CASCADE_PATH)
        val cachedFaceCascadeFile = cacheAssetFile(faceCascadeAssetUri)

        val faceCascadeClassifier = CascadeClassifier(cachedFaceCascadeFile.absolutePath)
        val isEmpty = faceCascadeClassifier.empty()

        if (isEmpty) {
            throw IOException("Cascade classifier is empty")
        }

        val faceMat = Mat()
        Utils.bitmapToMat(faceImageBitmap, faceMat)
        val faceRectMat = MatOfRect()

        faceCascadeClassifier.detectMultiScale(faceMat, faceRectMat)

        val faces = faceRectMat.toArray()

        faces.forEach { face ->
            val halfSize = Size((face.width / 2).toDouble(), (face.height / 2).toDouble())
            val center = Point(face.x.toDouble() + halfSize.width, face.y.toDouble() + halfSize.height)
            val color = Scalar(0.0, 0.0, 0.0, 0.0)

            Imgproc.ellipse(faceMat, center, halfSize, 0.0, 0.0, 360.0, color, 10, 8, 0)
        }

        Utils.matToBitmap(faceMat, faceImageBitmap)
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
}
