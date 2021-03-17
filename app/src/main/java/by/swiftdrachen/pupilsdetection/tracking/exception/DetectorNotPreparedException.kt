package by.swiftdrachen.pupilsdetection.tracking.exception

class DetectorNotPreparedException(reason: String) :
    Exception("Detector hasn't prepared yet (reason: ${reason})") {
}
