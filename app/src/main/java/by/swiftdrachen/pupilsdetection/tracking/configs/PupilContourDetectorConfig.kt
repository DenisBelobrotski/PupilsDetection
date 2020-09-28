package by.swiftdrachen.pupilsdetection.tracking.configs

import org.opencv.core.Scalar

class PupilContourDetectorConfig {
    val thresholdStartValue = 0
    val thresholdStep = 1
    val maxThreshold = 255
    val drawingContourIndex = -1 //all contours
    val contourColor = Scalar(255.0, 255.0, 255.0)
    val minSizeRate = 0.2 //0.25
    val maxSizeRate = 0.75 //0.41
    val maxAspectRate = 0.25 //0.2
    val maxAreaRate = 0.2
}
