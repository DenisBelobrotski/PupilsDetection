package by.swiftdrachen.pupilsdetection

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class FaceDetector(
    private val faceCascadeClassifier: CascadeClassifier,
    private val eyeCascadeClassifier: CascadeClassifier) {

    fun detectAndMarkFaces(sourceBitmap: Bitmap) {
        val sourceMat = Mat()
        Utils.bitmapToMat(sourceBitmap, sourceMat)
        detectAndMarkFaces(sourceMat)
        Utils.matToBitmap(sourceMat, sourceBitmap)
    }

    // TODO: Optimize memory allocation.
    //  Move MatOfRect() to class fields.
    //  See "MatOfRect.size().height.toInt()" and "MatOfRect.toArray()" implementation.
    //  Use "for (i in 0 until facesCount)".
    fun detectAndMarkFaces(sourceMat: Mat) {
        val faceRects = detectFaces(sourceMat)
        faceRects.forEach { faceRect ->
            markRect(sourceMat, faceRect, Point())

            val faceROI = sourceMat.submat(faceRect)
            val eyeRects = detectEyes(faceROI)
            eyeRects.forEach { eyeRect ->
                markRect(sourceMat, eyeRect, faceRect.tl())
            }
        }
    }

    fun detectFaces(sourceMat: Mat): Array<Rect> {
        val facesRectMat = MatOfRect()
        faceCascadeClassifier.detectMultiScale(sourceMat, facesRectMat)
        return facesRectMat.toArray()
    }

    fun detectEyes(sourceMat: Mat): Array<Rect> {
        val eyesRectMat = MatOfRect()
        eyeCascadeClassifier.detectMultiScale(sourceMat, eyesRectMat)
        return eyesRectMat.toArray()
    }

    fun detectFaces(sourceBitmap: Bitmap): Array<Rect> {
        val sourceMat = Mat()
        val facesRectMat = MatOfRect()

        Utils.bitmapToMat(sourceBitmap, sourceMat)
        faceCascadeClassifier.detectMultiScale(sourceMat, facesRectMat)

        return facesRectMat.toArray()
    }

    fun markRect(mat: Mat, rect: Rect, offset: Point) {
        val faceHalfSize = Size(
                rect.width.toDouble() * 0.5,
                rect.height.toDouble() * 0.5)
        val faceCenter = Point(
                offset.x + rect.x.toDouble() + faceHalfSize.width,
                offset.y + rect.y.toDouble() + faceHalfSize.height)
        val faceColor = Scalar(1.0, 1.0, 1.0, 1.0)

        Imgproc.circle(mat, faceCenter, rect.width / 2, faceColor, 10, 8, 0)
    }
}
