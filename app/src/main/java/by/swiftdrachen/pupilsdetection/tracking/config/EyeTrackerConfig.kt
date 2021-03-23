package by.swiftdrachen.pupilsdetection.tracking.config

import by.swiftdrachen.pupilsdetection.tracking.abstraction.IEyeTrackerConfig

class EyeTrackerConfig : IEyeTrackerConfig {
    override var mirrorEyes = false

    override var drawDebugFaceRects = true
    override var drawDebugEyeRects = true
    override var drawDebugEyeMarkers = true
}
