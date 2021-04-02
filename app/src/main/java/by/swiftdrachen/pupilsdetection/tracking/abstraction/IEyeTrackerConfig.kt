package by.swiftdrachen.pupilsdetection.tracking.abstraction

import by.swiftdrachen.pupilsdetection.tracking.detector.EyeProcessor
import by.swiftdrachen.pupilsdetection.tracking.util.SessionFileManager
import org.opencv.core.Point

interface IEyeTrackerConfig {
    val faceDetector: IRectDetector
    val eyeDetector: IRectDetector
    val eyeProcessor: IEyeProcessor
    var sessionFileManager: SessionFileManager?

    var grayscaleEnabled: Boolean
    var histogramEqualizationEnabled: Boolean

    var mirrorEyes: Boolean
    var gazeDirections: Array<Point>
    var gazeCenterDirectionOffset: Int

    var drawDebugFaceRects: Boolean
    var drawDebugEyeRects: Boolean
    var drawDebugEyeMarkers: Boolean
}
