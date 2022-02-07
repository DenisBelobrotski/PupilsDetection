package com.denisbelobrotski.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.denisbelobrotski.eye_tracking_library.cv_util.OpenCvUtils

class MainActivity : AppCompatActivity() {
    private val imageDetectorButton by lazy { findViewById<Button>(R.id.image_detector_button) }
    private val cameraDetectorButton by lazy { findViewById<Button>(R.id.camera_detector_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        OpenCvUtils.loadLibraries()

        imageDetectorButton.setOnClickListener {
            val imageDetectorIntent = Intent(this, ImageDetectorActivity::class.java)
            startActivity(imageDetectorIntent)
        }

        cameraDetectorButton.setOnClickListener {
            val intent = Intent(this, PreviewActivity::class.java)
            startActivity(intent)
        }
    }
}
