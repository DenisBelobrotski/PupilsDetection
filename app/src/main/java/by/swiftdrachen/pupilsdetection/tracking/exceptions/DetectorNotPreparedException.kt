package by.swiftdrachen.pupilsdetection.tracking.exceptions

import java.lang.Exception

class DetectorNotPreparedException(private val reason: String) :
    Exception("Detector hasn't prepared yet (reason: ${reason})") {
}
