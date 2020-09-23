package by.swiftdrachen.pupilsdetection.tracking.configs

interface CascadeClassifierConfig {
    val scaleFactor: Double
    val minNeighbours: Int
    val flags: Int
    val minSizeRatio: Double
    val maxSizeRatio: Double
}
