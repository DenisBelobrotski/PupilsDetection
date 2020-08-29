package by.swiftdrachen.pupilsdetection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber

class VideoDetectorActivity : DetectorActivity() {
    private val videoFileChooser by lazy { FileChooser(this, "video", "*") }
    private val videoView by lazy { findViewById<ImageView>(R.id.video_detector_image_view) }
    private val chooseVideoButton by lazy { findViewById<Button>(R.id.choose_video_button) }
    private val processVideoButton by lazy { findViewById<Button>(R.id.process_video_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_detector)

        chooseVideoButton.setOnClickListener {
            videoFileChooser.choose()
        }

        processVideoButton.setOnClickListener {
            processVideo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        videoFileChooser.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processVideo() {
        val userFileUri = videoFileChooser.lastChosenFileUri.value

        if (userFileUri == null) {
            Toast.makeText(this, "Video file wasn't chosen.", Toast.LENGTH_LONG).show();
            return
        }

        val userFileInputStream = FileSystemUtils.openUserFileInputStream(this, userFileUri)
        val grabber = FFmpegFrameGrabber(userFileInputStream)

        try {
            grabber.start()
        } catch (exception: FrameGrabber.Exception) {
            Toast.makeText(this, "Failed to start grabber.", Toast.LENGTH_LONG).show();
        }

        var frame: Frame? = null
        var framesCount = 0

        do {
            try {
                frame = grabber.grabFrame()
                if (frame != null) {
                    framesCount += 1
                }
            } catch (exception: FrameGrabber.Exception) {
                Toast.makeText(this, "Failed to grab frame.", Toast.LENGTH_LONG).show();
            }
        } while (frame != null)

        Toast.makeText(this,
            "video format: ${grabber.format}\n" +
                    "pixel format: ${grabber.pixelFormat}\n" +
                    "sample format: ${grabber.sampleFormat}",
            Toast.LENGTH_LONG).show();

        try {
            grabber.stop()
        } catch (exception: FrameGrabber.Exception) {
            Toast.makeText(this, "Failed to stop grabber.", Toast.LENGTH_LONG).show();
        }

        userFileInputStream?.close()

        Toast.makeText(this, "frames count: $framesCount", Toast.LENGTH_LONG).show();
    }
}
