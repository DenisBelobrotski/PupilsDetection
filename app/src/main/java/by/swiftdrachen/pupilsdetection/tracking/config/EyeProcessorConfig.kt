package by.swiftdrachen.pupilsdetection.tracking.config

import by.swiftdrachen.pupilsdetection.tracking.abstraction.IEyeProcessorConfig

class EyeProcessorConfig : IEyeProcessorConfig {
    override var topOffsetPercentage: Int = 40
    override var bottomOffsetPercentage: Int = 0
}
