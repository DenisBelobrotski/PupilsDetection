package by.swiftdrachen.pupilsdetection.tracking.abstraction

import by.swiftdrachen.pupilsdetection.tracking.util.SessionFileManager
import org.opencv.core.Mat
import org.opencv.core.Point

interface IEyeProcessor {
    val config: IEyeProcessorConfig
    val eyePreciser: IPointDetector
    val pupilDetector: IPointDetector

    var sourceImage: Mat?
    var sessionFileManager: SessionFileManager?

    val detectedEyeCenter: Point
    val detectedPupilCenter: Point

    fun process()
}
