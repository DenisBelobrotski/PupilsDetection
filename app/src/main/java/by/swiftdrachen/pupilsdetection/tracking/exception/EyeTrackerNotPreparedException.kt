package by.swiftdrachen.pupilsdetection.tracking.exception

class EyeTrackerNotPreparedException(reason: String) :
    Exception("Eye tracker hasn't prepared yet (reason: ${reason})") {
}
