package by.swiftdrachen.pupilsdetection.tracking.abstraction

import org.opencv.core.Point

interface IEyeTrackerConfig {
    var mirrorEyes: Boolean
    var gazeDirections: Array<Point>

    var drawDebugFaceRects: Boolean
    var drawDebugEyeRects: Boolean
    var drawDebugEyeMarkers: Boolean
}
