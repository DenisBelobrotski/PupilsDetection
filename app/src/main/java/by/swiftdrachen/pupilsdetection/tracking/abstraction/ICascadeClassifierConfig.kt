package by.swiftdrachen.pupilsdetection.tracking.abstraction

interface ICascadeClassifierConfig {
    val assetPath: String
    val scaleFactor: Double
    val minNeighbours: Int
    val flags: Int
    val minSizeRatio: Double
    val maxSizeRatio: Double
}
