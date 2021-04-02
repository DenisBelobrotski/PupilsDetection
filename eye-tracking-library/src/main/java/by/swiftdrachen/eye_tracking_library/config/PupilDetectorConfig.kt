package by.swiftdrachen.eye_tracking_library.config

import by.swiftdrachen.eye_tracking_library.abstraction.IPupilDetectorConfig

class PupilDetectorConfig : IPupilDetectorConfig {
    override var shouldEqualizeHistogram = true
    override var threshold = 10
    override var maxThreshold = 255
    override var erosionIterationsCount = 2
    override var dilationIterationsCount = 4
    override var isErosionEnabled = false
    override var isDilationEnabled = false
}
