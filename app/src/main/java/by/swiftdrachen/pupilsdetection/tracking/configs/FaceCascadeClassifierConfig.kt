package by.swiftdrachen.pupilsdetection.tracking.configs

class FaceCascadeClassifierConfig : CascadeClassifierConfig {
    override val scaleFactor: Double
        get() = 1.1

    override val minNeighbours: Int
        get() = 4

    override val flags: Int
        get() = 0

    override val minSizeRatio: Double
        get() = 0.0

    override val maxSizeRatio: Double
        get() = 1.0
}
