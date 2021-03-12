package by.swiftdrachen.pupilsdetection.tracking.exceptions

class EyeTrackerNotPreparedException(reason: String) :
    Exception("Eye tracker hasn't prepared yet (reason: ${reason})") {
}
