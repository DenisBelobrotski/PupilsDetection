package by.swiftdrachen.eye_tracking_library.detector

import by.swiftdrachen.eye_tracking_library.abstraction.PointDetector
import by.swiftdrachen.eye_tracking_library.cv_util.OpenCvUtils
import org.opencv.core.Mat
import org.opencv.core.Point

class EyePreciserCenter : PointDetector() {
    override fun getDetectedPoint(processingImage: Mat): Point {
        sessionFileManager?.addLog("EyePreciserCenter - detection started", true)

        val center = OpenCvUtils.getMatCenter(processingImage)

        sessionFileManager?.addLog("EyePreciserCenter - detection done", true)

        return center
    }
}
