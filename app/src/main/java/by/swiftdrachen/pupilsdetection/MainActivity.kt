package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.swiftdrachen.pupilsdetection.obsolete.VideoDetectorActivity
import by.swiftdrachen.pupilsdetection.tracking.cv_util.OpenCvUtils

class MainActivity : AppCompatActivity() {
    private val imageDetectorButton by lazy { findViewById<Button>(R.id.image_detector_button) }
    private val videoDetectorButton by lazy { findViewById<Button>(R.id.video_detector_button) }
    private val cameraDetectorButton by lazy { findViewById<Button>(R.id.camera_detector_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        OpenCvUtils.loadLibraries()

        imageDetectorButton.setOnClickListener {
            val imageDetectorIntent = Intent(this, ImageDetectorActivity::class.java)
            startActivity(imageDetectorIntent)
        }

        videoDetectorButton.setOnClickListener {
            val videoDetectorIntent = Intent(this, VideoDetectorActivity::class.java)
            startActivity(videoDetectorIntent)
        }

        cameraDetectorButton.setOnClickListener {
            Toast.makeText(this, "Camera detector is not implemented yet.", Toast.LENGTH_LONG).show()
        }
    }
}
