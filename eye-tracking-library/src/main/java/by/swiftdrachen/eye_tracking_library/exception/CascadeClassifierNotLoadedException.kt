package by.swiftdrachen.eye_tracking_library.exception

class CascadeClassifierNotLoadedException(name: String) :
    Exception("Cascade classifier ${name} not loaded.") {
}
