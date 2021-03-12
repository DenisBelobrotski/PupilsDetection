package by.swiftdrachen.pupilsdetection.tracking.exceptions

class CascadeClassifierNotLoadedException(name: String) :
    Exception("Cascade classifier ${name} not loaded.") {
}
