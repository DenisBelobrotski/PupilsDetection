package by.swiftdrachen.pupilsdetection.obsolete

import android.util.Log
import by.swiftdrachen.pupilsdetection.utils.TimeSpan
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.*

class VideoFaceDetector(
        private val videoInputStream: InputStream,
        private val videoOutputFile: File,
        private val faceDetector: FaceAndEyesDetector,
        private val isFlipped: Boolean = false) : Closeable {

    private val grabber = FFmpegFrameGrabber(videoInputStream)
    private val recorder: FFmpegFrameRecorder

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
        var framesCount = 0
        var emptyFramesCount = 0
        var frame: Frame? = grabber.grab()
        var frameMat: org.opencv.core.Mat? = null

        val startTime = Calendar.getInstance().time

        while (frame != null) {
            Log.d("FACE_DETECT", "Start processing frame at index $framesCount")

            frameMat = frameToMat(frame)

            if (frameMat != null) {
                faceDetector.detectAndMarkFaces(frameMat)
                frame = matToFrame(frameMat)
            } else {
                emptyFramesCount += 1
            }

            recorder.record(frame)

            frame = grabber.grab()
            framesCount++
        }

        val deltaTime = Calendar.getInstance().time.time - startTime.time
        val processingDuration = TimeSpan(deltaTime)

        Log.d("FACE_DETECT", "Processing duration: $processingDuration")
        Log.d("FACE_DETECT", "Video frames count: ${grabber.lengthInVideoFrames}")
        Log.d("FACE_DETECT", "Audio frames count: ${grabber.lengthInAudioFrames}")
        Log.d("FACE_DETECT", "Processed frames count: $framesCount")
        Log.d("FACE_DETECT", "Empty frames count: $emptyFramesCount")
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