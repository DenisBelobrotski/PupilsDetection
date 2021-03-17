package by.swiftdrachen.pupilsdetection.tracking.configs

class PupilDetectorConfig {
    val shouldEqualizeHistogram = true
    val threshold = 10
    val maxThreshold = 255
    val erosionIterationsCount = 2
    val dilationIterationsCount = 4
    val isErosionEnabled = false
    val isDilationEnabled = false
}
