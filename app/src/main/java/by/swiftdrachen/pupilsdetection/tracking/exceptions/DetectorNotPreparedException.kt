package by.swiftdrachen.pupilsdetection.tracking.exceptions

class DetectorNotPreparedException(reason: String) :
    Exception("Detector hasn't prepared yet (reason: ${reason})") {
}
