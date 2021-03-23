package by.swiftdrachen.pupilsdetection.tracking.config

import by.swiftdrachen.pupilsdetection.tracking.abstraction.IEyeTrackerConfig
import org.opencv.core.Point

class EyeTrackerConfig : IEyeTrackerConfig {
    override var mirrorEyes = false
    override var gazeDirections = arrayOf(
            Point(-1.0, 0.0), //left
            Point(1.0, 0.0), //right
            Point(0.0, -1.0), //top
            Point(0.0, 1.0), //bottom
    )

    override var drawDebugFaceRects = true
    override var drawDebugEyeRects = true
    override var drawDebugEyeMarkers = true

    var gazeDirectionName = arrayOf(
            "left",
            "right",
            "top",
            "bottom",
    )
    var gazeDirectionCenterName = "center"
    var gazeDirectionNoResultName = "not detected"
}
