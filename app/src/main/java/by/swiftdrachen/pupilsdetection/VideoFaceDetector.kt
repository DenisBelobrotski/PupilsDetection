package by.swiftdrachen.pupilsdetection

import android.util.Log
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.io.Closeable
import java.io.File
import java.io.InputStream

class VideoFaceDetector(
        private val videoInputStream: InputStream,
        private val videoOutputFile: File,
        private val faceDetector: FaceDetector,
        private val isFlipped: Boolean = false) : Closeable {

    private val grabber = FFmpegFrameGrabber(videoInputStream)
    private val recorder: FFmpegFrameRecorder;

    private var badFramesCount = 0

    private val javaCvMatToOpenCvMatConverter = OpenCVFrameConverter.ToOrgOpenCvCoreMat()

    private var flippedMatDestination: org.opencv.core.Mat? = null
    private var rotationMat: org.opencv.core.Mat? = null

    init {
        grabber.start()

        recorder = getRecorderByGrabber(grabber)
        recorder.start()

        if (isFlipped) {
            flippedMatDestination = createFlippedMatDestination()
            rotationMat = getRotationMatrix(grabber.imageWidth, grabber.imageHeight, Math.toRadians(90.0))
        }
    }

    override fun close() {
        grabber.stop()
        recorder.stop()
    }

    private fun getRecorderByGrabber(targetGrabber: FFmpegFrameGrabber): FFmpegFrameRecorder {
        val resultRecorder = FFmpegFrameRecorder(videoOutputFile, targetGrabber.imageWidth, targetGrabber.imageHeight)
        resultRecorder.videoCodec = targetGrabber.videoCodec
        resultRecorder.format = targetGrabber.format
        resultRecorder.audioChannels = targetGrabber.audioChannels
        resultRecorder.audioCodec = targetGrabber.audioCodec
        resultRecorder.audioBitrate = targetGrabber.audioBitrate
        resultRecorder.frameRate = targetGrabber.frameRate
        resultRecorder.videoBitrate = targetGrabber.videoBitrate

        return resultRecorder
    }

    fun detectFaces() {
        for (frameIndex in 0 until grabber.lengthInFrames) {
            Log.d("FACE_DETECT", "Start processing frame at index $frameIndex")

            val frameMat = getFrameMatAtIndex(frameIndex)

            if (frameMat == null) {
                badFramesCount += 1
                Log.d("VIDEO_RECORDING", "frame at index $frameIndex skipped!")
                continue
            }

            //TODO: flip frame correctly
//            if (isFlipped && flippedMatDestination != null && rotationMat != null) {
//                Imgproc.warpAffine(frameMat, flippedMatDestination, rotationMat, frameMat.size())
//                frameMat = flippedMatDestination
//            }

            faceDetector.detectAndMarkFaces(frameMat)

            recordMat(frameMat)
        }

        Log.d("FACE_DETECT", "bad frames count: $badFramesCount")
    }

    private fun frameToMat(frame: Frame?): org.opencv.core.Mat? {
        if (frame?.image == null) {
            return null
        }

        return javaCvMatToOpenCvMatConverter.convert(frame)
    }

    private fun matToFrame(mat: org.opencv.core.Mat?): Frame? {
        if (mat == null) {
            return null
        }

        return javaCvMatToOpenCvMatConverter.convert(mat)
    }

    private fun getFrameAtIndex(frameIndex: Int): Frame? {
        var frame: Frame? = null

        if (frameIndex >= 0 && frameIndex < grabber.lengthInFrames) {
            grabber.frameNumber = frameIndex
            frame = grabber.grabImage()
        }

        return frame
    }

    private fun getFrameMatAtIndex(frameIndex: Int): org.opencv.core.Mat? {
        var frameMat: org.opencv.core.Mat? = null

        val frame = getFrameAtIndex(frameIndex)
        frame?.let {
            frameMat = frameToMat(frame)
        }

        return frameMat
    }

    private fun recordMat(mat: org.opencv.core.Mat) {
        val frame = matToFrame(mat)
        recorder.record(frame)
    }

    private fun createFlippedMatDestination(): org.opencv.core.Mat? {
        var flippedMat: org.opencv.core.Mat? = null
        val firstFrameMat = getFrameMatAtIndex(0)
        firstFrameMat?.let {
            flippedMat = org.opencv.core.Mat(firstFrameMat.cols(), firstFrameMat.rows(), firstFrameMat.type())
        }

        return flippedMat
    }

    private fun getRotationMatrix(rows: Int, columns: Int, angle: Double): org.opencv.core.Mat {
        val center = Point((columns / 2).toDouble(), (rows / 2).toDouble())
        return Imgproc.getRotationMatrix2D(center, angle, 1.0)
    }
}