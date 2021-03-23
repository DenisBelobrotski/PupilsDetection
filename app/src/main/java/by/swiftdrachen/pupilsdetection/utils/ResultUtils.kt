package by.swiftdrachen.pupilsdetection.utils

import android.widget.TextView
import by.swiftdrachen.pupilsdetection.tracking.algorithm.EyeTracker
import by.swiftdrachen.pupilsdetection.tracking.config.EyeTrackerConfig

class ResultUtils {
    companion object {
        fun updateEyeStatus(textView: TextView, directionIndex: Int, config: EyeTrackerConfig) {
            textView.text = when {
                directionIndex >= 0 -> config.gazeDirectionNames[directionIndex]
                directionIndex == EyeTracker.CENTER_GAZE_DIRECTION_INDEX -> config.gazeDirectionCenterName
                directionIndex == EyeTracker.DEFAULT_GAZE_DIRECTION_INDEX -> config.gazeDirectionNoResultName
                else -> config.gazeDirectionNoResultName
            }
        }
    }
}
