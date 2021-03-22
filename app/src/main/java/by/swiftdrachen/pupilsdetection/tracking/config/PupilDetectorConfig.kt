package by.swiftdrachen.pupilsdetection.tracking.config

class PupilDetectorConfig {
    var shouldEqualizeHistogram = true
    var threshold = 10
    var maxThreshold = 255
    var erosionIterationsCount = 2
    var dilationIterationsCount = 4
    var isErosionEnabled = false
    var isDilationEnabled = false
}
