package by.swiftdrachen.pupilsdetection.tracking.exceptions

import java.lang.Exception

class CascadeClassifierNotLoadedException(name: String) :
    Exception("Cascade classifier ${name} not loaded.") {
}
