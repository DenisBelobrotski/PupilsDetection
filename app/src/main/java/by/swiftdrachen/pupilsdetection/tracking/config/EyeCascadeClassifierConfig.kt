package by.swiftdrachen.pupilsdetection.tracking.config

import by.swiftdrachen.pupilsdetection.tracking.abstraction.ICascadeClassifierConfig

class EyeCascadeClassifierConfig : ICascadeClassifierConfig {
    override val scaleFactor: Double
        get() = 1.3

    override val minNeighbours: Int
        get() = 5

    override val flags: Int
        get() = 0

    override val minSizeRatio: Double
        get() = 0.0

    override val maxSizeRatio: Double
        get() = 1.0
}
