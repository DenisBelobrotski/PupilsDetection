package by.swiftdrachen.pupilsdetection.tracking.exception

class CascadeClassifierNotLoadedException(name: String) :
    Exception("Cascade classifier ${name} not loaded.") {
}
