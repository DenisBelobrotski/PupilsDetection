package by.swiftdrachen.pupilsdetection

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class FaceDetector(
    private val faceCascadeClassifier: CascadeClassifier,
    private val eyeCascadeClassifier: CascadeClassifier) {

    fun processBitmap(sourceBitmap: Bitmap) {
        val processingImage = Mat()
        Utils.bitmapToMat(sourceBitmap, processingImage)

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

        Utils.matToBitmap(processingImage, sourceBitmap)
    }
}
