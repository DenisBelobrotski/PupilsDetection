package by.swiftdrachen.pupilsdetection.tracking.exceptions

import java.lang.Exception

class EyeTrackerNotPreparedException(reason: String) :
    Exception("Eye tracker hasn't prepared yet (reason: ${reason})") {
}
